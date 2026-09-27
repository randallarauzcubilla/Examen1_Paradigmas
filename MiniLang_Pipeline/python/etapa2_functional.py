"""Functional execution stage of the MiniLang pipeline.

This module is Stage 2 of the MiniLang polyglot pipeline:
it reads the Intermediate Representation (programa.ir)
produced by the Java stage, executes the instructions in
a purely functional style, and writes the execution trace
and final result to resultado.txt.

Functional guarantees:
    - No for/while loops in the core logic.
    - filter(), map() and functools.reduce() drive all
      list transformations and aggregations.
    - The whole program runs as a single fold (reduce)
      over the instruction list.

IR contract (defined with the Java stage):
    DATA: n1,n2,n3
    FILTER: op,value
    MAP: op,value
    REDUCE: OP
    PRINT:

Authors:
    Randall AC
    Keilor MC
"""

import sys
from collections import namedtuple
from functools import reduce
from pathlib import Path

DEFAULT_INPUT = "programa.ir"
DEFAULT_OUTPUT = "resultado.txt"
SEARCH_DIRS = ("output", ".")

State = namedtuple("State", ["value", "trace"])


class PipelineError(Exception):
    """Controlled error for invalid IR content."""


def _to_int(text, line_no):
    """Convert text to int or raise a controlled error.

    Args:
        text: raw string to convert.
        line_no: IR line number for error reporting.

    Returns:
        The parsed integer.

    Raises:
        PipelineError: if text is not a valid integer.
    """
    try:
        return int(text.strip())
    except ValueError:
        raise PipelineError(
            f"Line {line_no}: bad integer '{text}'")


def _fmt(value):
    """Format a numeric value without trailing .0.

    Args:
        value: int or float to format.

    Returns:
        Clean string representation.
    """
    if isinstance(value, float) and value.is_integer():
        return str(int(value))
    return str(value)


def _fmt_list(values):
    """Format a list of numbers as [a, b, c].

    Args:
        values: list of integers.

    Returns:
        Bracketed comma-separated string.
    """
    return "[" + ", ".join(map(str, values)) + "]"


COMPARISONS = {
    ">": lambda a, b: a > b,
    "<": lambda a, b: a < b,
    ">=": lambda a, b: a >= b,
    "<=": lambda a, b: a <= b,
    "==": lambda a, b: a == b,
    "=": lambda a, b: a == b,
    "!=": lambda a, b: a != b,
}

ARITHMETIC = {
    "+": lambda a, b: a + b,
    "-": lambda a, b: a - b,
    "*": lambda a, b: a * b,
    "/": lambda a, b: int(a / b),
}


def _reduce_sum(values):
    """Sum all values using reduce.

    Args:
        values: list of integers.

    Returns:
        The total (0 for an empty list).
    """
    return reduce(lambda acc, x: acc + x, values, 0)


def _reduce_max(values):
    """Maximum value using reduce.

    Args:
        values: list of integers (must not be empty).

    Returns:
        The greatest element.

    Raises:
        PipelineError: if the list is empty.
    """
    if not values:
        raise PipelineError("REDUCE MAX on empty list")
    return reduce(lambda a, x: a if a > x else x, values)


def _reduce_min(values):
    """Minimum value using reduce.

    Args:
        values: list of integers (must not be empty).

    Returns:
        The smallest element.

    Raises:
        PipelineError: if the list is empty.
    """
    if not values:
        raise PipelineError("REDUCE MIN on empty list")
    return reduce(lambda a, x: a if a < x else x, values)


def _reduce_avg(values):
    """Average value using reduce.

    Args:
        values: list of integers (must not be empty).

    Returns:
        The arithmetic mean as float.

    Raises:
        PipelineError: if the list is empty.
    """
    if not values:
        raise PipelineError("REDUCE AVG on empty list")
    total = reduce(lambda acc, x: acc + x, values, 0)
    return total / len(values)


REDUCERS = {
    "SUM": _reduce_sum,
    "MAX": _reduce_max,
    "MIN": _reduce_min,
    "AVG": _reduce_avg,
}


def _require_list(state, line_no, keyword):
    """Ensure the current value is a list of numbers.

    Args:
        state: current pipeline state.
        line_no: IR line number for error reporting.
        keyword: instruction keyword being applied.

    Raises:
        PipelineError: if DATA is missing or a REDUCE
            already collapsed the value to a scalar.
    """
    if state.value is None:
        raise PipelineError(
            f"Line {line_no}: {keyword} before DATA")
    if not isinstance(state.value, list):
        raise PipelineError(
            f"Line {line_no}: {keyword} after REDUCE")


def _parse_int_list(payload, line_no):
    """Parse a comma separated integer list.

    Args:
        payload: raw payload text (e.g. '3,8,5').
        line_no: IR line number for error reporting.

    Returns:
        List of integers.
    """
    parts = payload.split(",")
    return list(map(lambda p: _to_int(p, line_no), parts))


def _parse_op_payload(payload, line_no):
    """Parse an 'operator,value' payload.

    Args:
        payload: raw payload text (e.g. '>,5').
        line_no: IR line number for error reporting.

    Returns:
        Tuple (operator, integer value).

    Raises:
        PipelineError: if the separator is missing.
    """
    op, sep, num = payload.partition(",")
    if not sep:
        raise PipelineError(
            f"Line {line_no}: expected 'op,value'")
    return op.strip(), _to_int(num, line_no)


def _handle_data(state, payload, line_no):
    """Execute a DATA instruction.

    Args:
        state: current pipeline state.
        payload: comma separated integers.
        line_no: IR line number.

    Returns:
        New state with the initial list.

    Raises:
        PipelineError: on duplicate or empty DATA.
    """
    if state.value is not None:
        raise PipelineError(
            f"Line {line_no}: duplicate DATA")
    values = _parse_int_list(payload, line_no)
    if not values:
        raise PipelineError(
            f"Line {line_no}: DATA list is empty")
    trace = state.trace + [
        f"STEP 0 DATA: {_fmt_list(values)}"]
    return State(values, trace)


def _handle_filter(state, payload, line_no):
    """Execute a FILTER instruction using filter().

    Args:
        state: current pipeline state.
        payload: 'operator,value' text.
        line_no: IR line number.

    Returns:
        New state with the filtered list.

    Raises:
        PipelineError: on unknown operator or bad state.
    """
    _require_list(state, line_no, "FILTER")
    op, num = _parse_op_payload(payload, line_no)
    predicate = COMPARISONS.get(op)
    if predicate is None:
        raise PipelineError(
            f"Line {line_no}: bad FILTER op '{op}'")
    result = list(
        filter(lambda x: predicate(x, num), state.value))
    step = len(state.trace)
    trace = state.trace + [
        f"STEP {step} FILTER {op} {num}: "
        f"{_fmt_list(result)}"]
    return State(result, trace)


def _handle_map(state, payload, line_no):
    """Execute a MAP instruction using map().

    Args:
        state: current pipeline state.
        payload: 'operator,value' text.
        line_no: IR line number.

    Returns:
        New state with the transformed list.

    Raises:
        PipelineError: on unknown operator, division by
            zero, or bad state.
    """
    _require_list(state, line_no, "MAP")
    op, num = _parse_op_payload(payload, line_no)
    operation = ARITHMETIC.get(op)
    if operation is None:
        raise PipelineError(
            f"Line {line_no}: bad MAP op '{op}'")
    if op == "/" and num == 0:
        raise PipelineError(
            f"Line {line_no}: MAP division by zero")
    result = list(
        map(lambda x: operation(x, num), state.value))
    step = len(state.trace)
    trace = state.trace + [
        f"STEP {step} MAP {op} {num}: "
        f"{_fmt_list(result)}"]
    return State(result, trace)


def _handle_reduce(state, payload, line_no):
    """Execute a REDUCE instruction using reduce().

    Args:
        state: current pipeline state.
        payload: reduce operator name (SUM/MAX/MIN/AVG).
        line_no: IR line number.

    Returns:
        New state with the scalar result.

    Raises:
        PipelineError: on unknown operator or bad state.
    """
    _require_list(state, line_no, "REDUCE")
    op = payload.strip().upper()
    reducer = REDUCERS.get(op)
    if reducer is None:
        raise PipelineError(
            f"Line {line_no}: bad REDUCE op '{op}'")
    result = reducer(state.value)
    step = len(state.trace)
    trace = state.trace + [
        f"STEP {step} REDUCE {op}: {_fmt(result)}"]
    return State(result, trace)


def _handle_print(state, payload, line_no):
    """Execute a PRINT instruction.

    Args:
        state: current pipeline state.
        payload: unused (PRINT has no arguments).
        line_no: IR line number.

    Returns:
        New state with the RESULT line appended.

    Raises:
        PipelineError: if there is nothing to print.
    """
    if state.value is None:
        raise PipelineError(
            f"Line {line_no}: PRINT with no data")
    trace = state.trace + [
        f"RESULT: {_fmt(state.value)}"]
    return State(state.value, trace)


HANDLERS = {
    "DATA": _handle_data,
    "FILTER": _handle_filter,
    "MAP": _handle_map,
    "REDUCE": _handle_reduce,
    "PRINT": _handle_print,
}


def _apply(state, instruction):
    """Apply one parsed instruction to the state.

    Args:
        state: current pipeline state.
        instruction: tuple (line_no, keyword, payload).

    Returns:
        The next pipeline state.

    Raises:
        PipelineError: on unknown instruction keyword.
    """
    line_no, keyword, payload = instruction
    handler = HANDLERS.get(keyword)
    if handler is None:
        raise PipelineError(
            f"Line {line_no}: unknown instruction "
            f"'{keyword}'")
    return handler(state, payload, line_no)


def _parse_line(line, line_no):
    """Parse one IR line into a typed tuple.

    Args:
        line: raw IR line text.
        line_no: 1-based line number.

    Returns:
        Tuple (line_no, keyword, payload), or None for
        blank lines.

    Raises:
        PipelineError: if the ':' separator is missing.
    """
    text = line.strip()
    if not text:
        return None
    keyword, sep, payload = text.partition(":")
    if not sep:
        raise PipelineError(
            f"Line {line_no}: missing ':' separator")
    return (line_no, keyword.strip().upper(),
            payload.strip())


def _resolve_path(path):
    """Locate a file using fallback directories.

    Args:
        path: requested file path or bare filename.

    Returns:
        A Path object pointing to an existing file.

    Raises:
        PipelineError: if the file is not found anywhere.
    """
    direct = Path(path)
    if direct.exists():
        return direct
    found = next(
        (Path(d) / direct.name for d in SEARCH_DIRS
         if (Path(d) / direct.name).exists()),
        None)
    if found is not None:
        print(f"[INFO] Found file at: {found}")
        return found
    raise PipelineError(f"File not found: {path}")


def read_instructions(path):
    """Read and parse the IR file.

    Args:
        path: IR file path or bare filename.

    Returns:
        List of parsed instruction tuples.

    Raises:
        PipelineError: on malformed IR content.
        OSError: if the file cannot be read.
    """
    resolved = _resolve_path(path)
    lines = resolved.read_text(
        encoding="utf-8").splitlines()
    numbered = enumerate(lines, start=1)
    parsed = map(
        lambda pair: _parse_line(pair[1], pair[0]),
        numbered)
    return list(filter(None, parsed))


def execute(instructions):
    """Run the whole program as a single fold.

    Args:
        instructions: list of parsed instruction tuples.

    Returns:
        The execution trace as a list of lines.

    Raises:
        PipelineError: on any execution error.
    """
    if not instructions:
        raise PipelineError("Empty IR program")
    final_state = reduce(
        _apply, instructions, State(None, []))
    return final_state.trace


def _write_output(path, trace):
    """Write the trace to the result file.

    Args:
        path: output path or bare filename.
        trace: list of trace lines.

    Raises:
        OSError: if the file cannot be written.
    """
    target = Path(path)
    if str(target.parent) == ".":
        out_dir = Path("output")
        out_dir.mkdir(exist_ok=True)
        target = out_dir / path
    target.write_text(
        "\n".join(trace) + "\n", encoding="utf-8")
    print(f"[INFO] File written to: {target.absolute()}")


def main(argv):
    """Entry point of Stage 2.

    Args:
        argv: command line arguments (optional input
            and output paths).

    Returns:
        Process exit code (0 success, 1 failure).
    """
    input_path = argv[1] if len(argv) > 1 \
        else DEFAULT_INPUT
    output_path = argv[2] if len(argv) > 2 \
        else DEFAULT_OUTPUT

    print("=== MiniLang Pipeline - Stage 2 (Python) ===")
    print(f"Reading: {input_path}")
    try:
        instructions = read_instructions(input_path)
        trace = execute(instructions)
        _write_output(output_path, trace)
        print(f"[OK] {len(instructions)} instructions "
              f"executed")
        print("=== Stage 2 completed successfully ===")
        return 0
    except PipelineError as err:
        print(f"PIPELINE ERROR: {err}", file=sys.stderr)
        return 1
    except OSError as err:
        print(f"FILE ERROR: {err}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))