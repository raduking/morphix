#!/usr/bin/env python3
"""
Print JaCoCo coverage summary for one or more classes from jacoco.csv.

Usage:
  # after running: mvn test
  ./scripts/jacoco-class-coverage.py org.morphix.lang.retry.DelayStrategy

  # multiple classes
  ./scripts/jacoco-class-coverage.py \
    org.morphix.lang.retry.DelayStrategy \
    org.morphix.lang.retry.delay.FixedDelayStrategy

  # custom jacoco csv path
  ./scripts/jacoco-class-coverage.py -c target/site/jacoco.csv org.example.MyClass

  # list all classes in the report
  ./scripts/jacoco-class-coverage.py --list
"""

import argparse
import csv
import pathlib
import sys


DEFAULT_CSV = "target/site/jacoco.csv"


def coverage_percentage(covered: int, missed: int) -> float:
    total = covered + missed
    return 100.0 if total == 0 else covered * 100.0 / total


def resolve_csv_path(csv_arg: str) -> pathlib.Path:
    """
    Resolve CSV path so the script works both from project root and from scripts/.

    Resolution order:
      1) exact path as provided
      2) current working directory + path
      3) project root (parent of this script dir) + path
    """
    p = pathlib.Path(csv_arg)

    # 1) as provided (absolute or relative to CWD)
    if p.exists():
        return p

    # 2) explicit CWD join (kept for clarity)
    cwd_candidate = pathlib.Path.cwd() / csv_arg
    if cwd_candidate.exists():
        return cwd_candidate

    # 3) project root candidate (script lives in <root>/scripts)
    project_root = pathlib.Path(__file__).resolve().parent.parent
    root_candidate = project_root / csv_arg
    if root_candidate.exists():
        return root_candidate

    return p


def load_rows(csv_path: pathlib.Path) -> list[dict[str, str]]:
    if not csv_path.exists():
        raise FileNotFoundError(
            f"Coverage file not found: {csv_path}. "
            f"Run tests first, e.g. 'mvn test'."
        )
    with csv_path.open(newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def full_class_name_from_row(row: dict[str, str]) -> str:
    return f"{row['PACKAGE']}.{row['CLASS']}"


def print_class_coverage(class_name: str, row: dict[str, str]) -> None:
    mi = int(row["INSTRUCTION_MISSED"])
    ci = int(row["INSTRUCTION_COVERED"])
    bm = int(row["BRANCH_MISSED"])
    bc = int(row["BRANCH_COVERED"])
    lm = int(row["LINE_MISSED"])
    lc = int(row["LINE_COVERED"])
    mm = int(row["METHOD_MISSED"])
    mc = int(row["METHOD_COVERED"])

    print(class_name)
    print(f"  instructions: {ci}/{ci + mi} ({coverage_percentage(ci, mi):.1f}%)")
    print(f"  branches:     {bc}/{bc + bm} ({coverage_percentage(bc, bm):.1f}%)")
    print(f"  lines:        {lc}/{lc + lm} ({coverage_percentage(lc, lm):.1f}%)")
    print(f"  methods:      {mc}/{mc + mm} ({coverage_percentage(mc, mm):.1f}%)")


def main() -> int:
    parser = argparse.ArgumentParser(description="Query class coverage from JaCoCo CSV report.")
    parser.add_argument("classes", nargs="*", help="Fully-qualified class names to query.")
    parser.add_argument(
        "-c",
        "--csv",
        default=DEFAULT_CSV,
        help=f"Path to jacoco.csv (default: {DEFAULT_CSV})",
    )
    parser.add_argument("--list", action="store_true", help="List all classes available in the CSV.")

    args = parser.parse_args()

    csv_path = resolve_csv_path(args.csv)
    rows = load_rows(csv_path)
    by_class = {full_class_name_from_row(r): r for r in rows}

    if args.list:
        for name in sorted(by_class):
            print(name)
        return 0

    if not args.classes:
        parser.error("Provide at least one class name, or use --list.")

    missing = []
    for class_name in args.classes:
        row = by_class.get(class_name)
        if row is None:
            missing.append(class_name)
            continue
        print_class_coverage(class_name, row)

    if missing:
        print("\nClass(es) not found in report:", file=sys.stderr)
        for m in missing:
            print(f"  - {m}", file=sys.stderr)
        return 2

    return 0


if __name__ == "__main__":
    sys.exit(main())
