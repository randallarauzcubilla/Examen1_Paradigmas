#=====================================================================
# MiniLang Pipeline - Stage 3: MIPS verification checksum
#
# Reads output/resultado.txt produced by the Python stage,
# extracts the final result and the number of executed
# operations, and computes the verification checksum:
#
#     checksum = resultado
#     checksum = checksum XOR operaciones
#     checksum = checksum + 17
#
# The values are written to output/firma.txt.
#
# Required elements covered:
#   - Registers            : $s0-$s7, $t0-$t9
#   - Memory access        : lb / sb / sw over buffers
#   - Loop / traversal     : buffer scan, itoa, strlen
#   - Arithmetic operation : add, sub, mul, div
#   - Logical operation    : xor
#   - Conditional branch   : beq / bne / bge / bltz
#
# @author Randall AC
# @author Keilor MC
#=====================================================================

        .data
in_name:    .asciiz "output/resultado.txt"
out_name:   .asciiz "output/firma.txt"
buffer:     .space 2048
numbuf:     .space 24
pat_step:   .asciiz "STEP"
# FIX 1: Strict pattern "RESULT:" (with colon) to avoid matching "RESULTADO"
pat_result: .asciiz "RESULT:"
lbl_res:    .asciiz "RESULT: "
lbl_ops:    .asciiz "OPERATIONS: "
lbl_byt:    .asciiz "BYTESUM: "
lbl_chk:    .asciiz "CHECKSUM: "
lbl_ok:     .asciiz "FIRMA: OK\n"
nl:         .asciiz "\n"
msg_start:  .asciiz "=== MiniLang Pipeline - Stage 3 (MIPS) ===\n"
msg_res:    .asciiz "[OK] Result read: "
msg_ops:    .asciiz "[OK] Operations: "
msg_chk:    .asciiz "[OK] Checksum: "
msg_done:   .asciiz "=== Stage 3 completed: firma.txt written ===\n"
err_open:   .asciiz "MIPS ERROR: cannot open resultado.txt\n"
err_empty:  .asciiz "MIPS ERROR: empty resultado.txt\n"
err_noops:  .asciiz "MIPS ERROR: no operations found\n"
err_out:    .asciiz "MIPS ERROR: cannot write firma.txt\n"

        .text
        .globl main

main:
        # ---------------- banner
        li   $v0, 4
        la   $a0, msg_start
        syscall

        # FIX 3: Delete stale firma.txt before starting
        # This prevents a valid signature from a previous run
        # from surviving if this execution fails.
        li   $v0, 13
        la   $a0, out_name
        li   $a1, 1              # write mode (to check existence)
        li   $a2, 0
        syscall
        bgez $v0, close_stale    # if file exists, close and delete
        j    open_input

close_stale:
        move $a0, $v0
        li   $v0, 16             # close file
        syscall
        # Note: MARS doesn't have a direct "delete" syscall,
        # but we'll overwrite it completely when we write.
        # The key is we don't leave a valid signature if we fail.

open_input:
        # ---------------- open resultado.txt (read)
        li   $v0, 13
        la   $a0, in_name
        li   $a1, 0
        li   $a2, 0
        syscall
        bltz $v0, fail_open
        move $s0, $v0

        # ---------------- read whole file into buffer
        li   $v0, 14
        move $a0, $s0
        la   $a1, buffer
        li   $a2, 2048
        syscall
        blez $v0, fail_empty
        move $s1, $v0

        # ---------------- close input file
        li   $v0, 16
        move $a0, $s0
        syscall

        # ---------------- scan buffer
        li   $t0, 0              # i = 0
        li   $s2, 0              # STEP counter
        li   $s3, 0              # byte sum
        li   $s4, 0              # final result
scan:
        bge  $t0, $s1, scan_end  # conditional branch
        la   $t1, buffer
        add  $t1, $t1, $t0
        lb   $t2, 0($t1)         # memory access
        addu $s3, $s3, $t2       # accumulate byte value

        # -------- match "STEP"
        move $a0, $t1
        la   $a1, pat_step
        jal  match_word
        beqz $v0, try_result
        addi $s2, $s2, 1
        j    advance

try_result:
        la   $t1, buffer
        add  $t1, $t1, $t0
        move $a0, $t1
        la   $a1, pat_result
        jal  match_word
        beqz $v0, advance
        # FIX 2: Offset corrected to 8 (R-E-S-U-L-T-:-space)
        addi $a0, $t0, 8
        jal  parse_int
        move $s4, $v0            # final result
        move $t0, $v1
        j    scan

advance:
        addi $t0, $t0, 1
        j    scan

scan_end:
        addi $s5, $s2, -1        # operations = STEP - DATA
        blez $s5, fail_noops

        # ---------------- checksum (PDF formula)
        move $s6, $s4
        xor  $s6, $s6, $s5       # logical operation
        addi $s6, $s6, 17        # arithmetic operation

        # ---------------- console evidence
        li   $v0, 4
        la   $a0, msg_res
        syscall
        li   $v0, 1
        move $a0, $s4
        syscall
        li   $v0, 4
        la   $a0, nl
        syscall
        li   $v0, 4
        la   $a0, msg_ops
        syscall
        li   $v0, 1
        move $a0, $s5
        syscall
        li   $v0, 4
        la   $a0, nl
        syscall
        li   $v0, 4
        la   $a0, msg_chk
        syscall
        li   $v0, 1
        move $a0, $s6
        syscall
        li   $v0, 4
        la   $a0, nl
        syscall

        # ---------------- open firma.txt (write)
        li   $v0, 13
        la   $a0, out_name
        li   $a1, 1
        li   $a2, 0
        syscall
        bltz $v0, fail_out
        move $s7, $v0

        # ---------------- write report pieces
        la   $a0, lbl_res
        jal  write_str
        move $a0, $s4
        jal  write_num
        la   $a0, nl
        jal  write_str
        la   $a0, lbl_ops
        jal  write_str
        move $a0, $s5
        jal  write_num
        la   $a0, nl
        jal  write_str
        la   $a0, lbl_byt
        jal  write_str
        move $a0, $s3
        jal  write_num
        la   $a0, nl
        jal  write_str
        la   $a0, lbl_chk
        jal  write_str
        move $a0, $s6
        jal  write_num
        la   $a0, nl
        jal  write_str
        la   $a0, lbl_ok
        jal  write_str

        # ---------------- close output file
        li   $v0, 16
        move $a0, $s7
        syscall

        li   $v0, 4
        la   $a0, msg_done
        syscall
        li   $v0, 17             # exit code 0
        li   $a0, 0
        syscall

#---------------------------------------------------------------------
# match_word: checks whether text starts with pattern.
#   $a0 = text address, $a1 = pattern address
#   returns $v0 = 1 on match, 0 otherwise
#---------------------------------------------------------------------
match_word:
        li   $v0, 1
mw_loop:
        lb   $t8, 0($a1)
        beqz $t8, mw_done
        lb   $t9, 0($a0)
        bne  $t8, $t9, mw_fail
        addi $a0, $a0, 1
        addi $a1, $a1, 1
        j    mw_loop
mw_fail:
        li   $v0, 0
mw_done:
        jr   $ra

#---------------------------------------------------------------------
# parse_int: parses an integer (optional '-') from the buffer.
#   $a0 = start index; returns $v0 = value, $v1 = next index
#---------------------------------------------------------------------
parse_int:
        li   $v0, 0
        li   $t4, 1              # sign
pi_skip:
        bge  $a0, $s1, pi_end
        la   $t1, buffer
        add  $t5, $t1, $a0
        lb   $t6, 0($t5)
        li   $t7, 45             # '-'
        beq  $t6, $t7, pi_neg
        li   $t7, 48
        blt  $t6, $t7, pi_adv
        li   $t7, 57
        ble  $t6, $t7, pi_digits
pi_adv:
        addi $a0, $a0, 1
        j    pi_skip
pi_neg:
        li   $t4, -1
        addi $a0, $a0, 1
pi_digits:
        bge  $a0, $s1, pi_end
        la   $t1, buffer
        add  $t5, $t1, $a0
        lb   $t6, 0($t5)
        li   $t7, 48
        blt  $t6, $t7, pi_end
        li   $t7, 57
        bgt  $t6, $t7, pi_end
        sub  $t6, $t6, 48
        mul  $v0, $v0, 10
        add  $v0, $v0, $t6
        addi $a0, $a0, 1
        j    pi_digits
pi_end:
        mul  $v0, $v0, $t4
        move $v1, $a0
        jr   $ra

#---------------------------------------------------------------------
# itoa: converts $a0 to a string inside numbuf.
#   returns $t2 = address of the first character
#---------------------------------------------------------------------
itoa:
        la   $t1, numbuf
        addi $t2, $t1, 22
        sb   $zero, 0($t2)
        li   $t3, 10
        move $t4, $a0
        li   $t5, 0
        bgez $t4, itoa_loop
        li   $t5, 1
        sub  $t4, $zero, $t4
itoa_loop:
        div  $t4, $t3
        mfhi $t6
        mflo $t4
        addi $t6, $t6, 48
        addi $t2, $t2, -1
        sb   $t6, 0($t2)
        bnez $t4, itoa_loop
        beqz $t5, itoa_done
        addi $t2, $t2, -1
        li   $t6, 45
        sb   $t6, 0($t2)
itoa_done:
        jr   $ra

#---------------------------------------------------------------------
# write_str: writes a NUL-terminated string to fd $s7.
#   $a0 = string address
#---------------------------------------------------------------------
write_str:
        addi $sp, $sp, -4
        sw   $ra, 0($sp)
        move $t1, $a0
ws_len:
        lb   $t2, 0($t1)
        beqz $t2, ws_go
        addi $t1, $t1, 1
        j    ws_len
ws_go:
        sub  $a2, $t1, $a0
        move $a1, $a0
        move $a0, $s7
        li   $v0, 15
        syscall
        lw   $ra, 0($sp)
        addi $sp, $sp, 4
        jr   $ra

#---------------------------------------------------------------------
# write_num: converts $a0 to text and writes it to fd $s7.
#---------------------------------------------------------------------
write_num:
        addi $sp, $sp, -4
        sw   $ra, 0($sp)
        jal  itoa
        move $a0, $t2
        jal  write_str
        lw   $ra, 0($sp)
        addi $sp, $sp, 4
        jr   $ra

#---------------------------------------------------------------------
# Failure exits with distinct codes for the orchestrator
#---------------------------------------------------------------------
fail_open:
        li   $v0, 4
        la   $a0, err_open
        syscall
        li   $v0, 17
        li   $a0, 1
        syscall
fail_empty:
        li   $v0, 4
        la   $a0, err_empty
        syscall
        li   $v0, 17
        li   $a0, 2
        syscall
fail_noops:
        li   $v0, 4
        la   $a0, err_noops
        syscall
        li   $v0, 17
        li   $a0, 3
        syscall
fail_out:
        li   $v0, 4
        la   $a0, err_out
        syscall
        li   $v0, 17
        li   $a0, 4
        syscall