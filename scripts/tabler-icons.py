#!/usr/bin/env python3
"""
Tabler Icons maintenance tool for SplitTrip.

Manages the hand-crafted Compose ImageVector icon constants generated from
Tabler SVGs. Supports adding, removing, updating, listing, and validating icons.

Usage:
    ./scripts/tabler-icons.py add    <style> <tabler-name> <KotlinName> [--file FILE]
    ./scripts/tabler-icons.py remove <style> <KotlinName>
    ./scripts/tabler-icons.py update <style> <tabler-name> <KotlinName>
    ./scripts/tabler-icons.py list   [--style <outline|filled>]
    ./scripts/tabler-icons.py check

Examples:
    # Add an outline icon to MiscIcons.kt (default)
    ./scripts/tabler-icons.py add outline heart Heart

    # Add an outline icon to a specific group file
    ./scripts/tabler-icons.py add outline heart Heart --file ContentIcons

    # Add a filled icon
    ./scripts/tabler-icons.py add filled heart HeartFilled

    # Remove an icon
    ./scripts/tabler-icons.py remove outline Heart

    # Re-download SVG and regenerate an icon
    ./scripts/tabler-icons.py update outline heart Heart

    # List all registered icons
    ./scripts/tabler-icons.py list

    # Check all icon files stay under 600 lines
    ./scripts/tabler-icons.py check

See wiki/tabler-icons-maintenance.md for full documentation.
"""

import argparse
import os
import re
import sys
import textwrap
from urllib.error import HTTPError
from urllib.request import urlopen

# ─── Constants ────────────────────────────────────────────────────────────────

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)

ICON_BASE_DIR = os.path.join(
    PROJECT_ROOT,
    "core", "design-system", "src", "main", "kotlin",
    "es", "pedrazamiguez", "splittrip", "core", "designsystem", "icon",
)
OUTLINE_DIR = os.path.join(ICON_BASE_DIR, "outline")
FILLED_DIR = os.path.join(ICON_BASE_DIR, "filled")

# Tabler Icons raw SVG download URL (GitHub main branch)
TABLER_RAW_URL = (
    "https://raw.githubusercontent.com/tabler/tabler-icons/main/icons/{style}/{name}.svg"
)

# Konsist file-size hard limit
MAX_LINES = 600

# Default target files when --file is not provided
DEFAULT_OUTLINE_FILE = "MiscIcons"
DEFAULT_FILLED_FILE = "FilledIcons"

BASE_PKG = "es.pedrazamiguez.splittrip.core.designsystem.icon"

OUTLINE_HEADER = textwrap.dedent("""\
    // {description}
    // Auto-generated from Tabler Icons (https://tabler.io/icons)
    // Do not edit manually
    @file:Suppress("MagicNumber", "LongMethod", "MaxLineLength")

    package {pkg}.outline

    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.SolidColor
    import androidx.compose.ui.graphics.StrokeCap
    import androidx.compose.ui.graphics.StrokeJoin
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.graphics.vector.addPathNodes
    import androidx.compose.ui.unit.dp
    import {pkg}.TablerIcons
""").format(pkg=BASE_PKG, description="{description}")

FILLED_HEADER = textwrap.dedent("""\
    // {description}
    // Auto-generated from Tabler Icons (https://tabler.io/icons)
    // Do not edit manually
    @file:Suppress("MagicNumber", "LongMethod", "MaxLineLength")

    package {pkg}.filled

    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.SolidColor
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.graphics.vector.addPathNodes
    import androidx.compose.ui.unit.dp
    import {pkg}.TablerIcons
""").format(pkg=BASE_PKG, description="{description}")

# Regex to identify the background/transparent path Tabler adds to every SVG
BACKGROUND_PATH = "M0 0h24v24H0z"


# ─── SVG Download & Parsing ──────────────────────────────────────────────────


def download_svg(tabler_name: str, style: str) -> str:
    """Download a Tabler SVG from GitHub."""
    url = TABLER_RAW_URL.format(style=style, name=tabler_name)
    print(f"  Downloading {url} …")
    try:
        with urlopen(url) as resp:
            content = resp.read().decode("utf-8")
    except HTTPError as exc:
        if exc.code == 404:
            print(f"  ✗ Icon '{tabler_name}' not found in Tabler ({style} style).")
            print(f"    Browse available icons at https://tabler.io/icons")
            sys.exit(1)
        raise
    return content


def extract_paths(svg_content: str) -> list[str]:
    """Extract path `d` attributes from SVG, skipping the background rectangle."""
    all_paths = re.findall(r'd="([^"]*)"', svg_content)
    # Filter out the transparent background path present in every Tabler SVG
    return [p for p in all_paths if BACKGROUND_PATH not in p]


# ─── Kotlin Code Generation ──────────────────────────────────────────────────


def generate_outline_icon(kotlin_name: str, paths: list[str]) -> str:
    """Generate an outline icon extension property."""
    lines = []
    lines.append(f"val TablerIcons.Outline.{kotlin_name}: ImageVector")
    lines.append(f"    get() = _{kotlin_name} ?: ImageVector.Builder(")
    lines.append(f'        name = "Outline.{kotlin_name}",')
    lines.append("        defaultWidth = 24.dp,")
    lines.append("        defaultHeight = 24.dp,")
    lines.append("        viewportWidth = 24f,")
    lines.append("        viewportHeight = 24f")

    for path_data in paths:
        escaped = path_data.replace("\\", "\\\\").replace('"', '\\"')
        lines.append("    ).addPath(")
        lines.append(f'        pathData = addPathNodes("{escaped}"),')
        lines.append("        stroke = SolidColor(Color.Black),")
        lines.append("        strokeLineWidth = 2f,")
        lines.append("        strokeLineCap = StrokeCap.Round,")
        lines.append("        strokeLineJoin = StrokeJoin.Round")

    lines.append(f"    ).build().also {{ _{kotlin_name} = it }}")
    lines.append("")
    lines.append(f"private var _{kotlin_name}: ImageVector? = null")
    return "\n".join(lines)


def generate_filled_icon(kotlin_name: str, paths: list[str]) -> str:
    """Generate a filled icon extension property."""
    lines = []
    lines.append(f"val TablerIcons.Filled.{kotlin_name}: ImageVector")
    lines.append(f"    get() = _{kotlin_name} ?: ImageVector.Builder(")
    lines.append(f'        name = "Filled.{kotlin_name}",')
    lines.append("        defaultWidth = 24.dp,")
    lines.append("        defaultHeight = 24.dp,")
    lines.append("        viewportWidth = 24f,")
    lines.append("        viewportHeight = 24f")

    for path_data in paths:
        escaped = path_data.replace("\\", "\\\\").replace('"', '\\"')
        lines.append("    ).addPath(")
        lines.append(f'        pathData = addPathNodes("{escaped}"),')
        lines.append("        fill = SolidColor(Color.Black)")

    lines.append(f"    ).build().also {{ _{kotlin_name} = it }}")
    lines.append("")
    lines.append(f"private var _{kotlin_name}: ImageVector? = null")
    return "\n".join(lines)


# ─── File Operations ──────────────────────────────────────────────────────────


def resolve_icon_dir(style: str) -> str:
    return OUTLINE_DIR if style == "outline" else FILLED_DIR


def resolve_target_file(style: str, file_name: str | None) -> str:
    """Resolve the full path to the target Kotlin file."""
    if file_name is None:
        file_name = DEFAULT_OUTLINE_FILE if style == "outline" else DEFAULT_FILLED_FILE
    if not file_name.endswith(".kt"):
        file_name += ".kt"
    return os.path.join(resolve_icon_dir(style), file_name)


def file_line_count(path: str) -> int:
    with open(path) as f:
        return sum(1 for _ in f)


def icon_exists_in_file(filepath: str, kotlin_name: str, style: str) -> bool:
    """Check if an icon with the given name already exists in a file."""
    style_obj = "Outline" if style == "outline" else "Filled"
    marker = f"val TablerIcons.{style_obj}.{kotlin_name}: ImageVector"
    if not os.path.exists(filepath):
        return False
    with open(filepath) as f:
        return marker in f.read()


def find_icon_file(style: str, kotlin_name: str) -> str | None:
    """Search all icon files in the style directory for a given icon name."""
    icon_dir = resolve_icon_dir(style)
    if not os.path.isdir(icon_dir):
        return None
    for fname in sorted(os.listdir(icon_dir)):
        if fname.endswith(".kt"):
            fpath = os.path.join(icon_dir, fname)
            if icon_exists_in_file(fpath, kotlin_name, style):
                return fpath
    return None


def remove_icon_block(filepath: str, kotlin_name: str, style: str) -> bool:
    """Remove an icon's code block (property + backing field) from a file."""
    style_obj = "Outline" if style == "outline" else "Filled"
    prop_marker = f"val TablerIcons.{style_obj}.{kotlin_name}: ImageVector"
    field_marker = f"private var _{kotlin_name}: ImageVector? = null"

    with open(filepath) as f:
        lines = f.readlines()

    # Find the start and end lines of the icon block
    start_idx = None
    end_idx = None
    for i, line in enumerate(lines):
        if prop_marker in line:
            start_idx = i
        if field_marker in line and start_idx is not None:
            end_idx = i
            break

    if start_idx is None or end_idx is None:
        return False

    # Include any blank line before the icon block (separator)
    if start_idx > 0 and lines[start_idx - 1].strip() == "":
        start_idx -= 1

    # Include blank line after the backing field if present
    if end_idx + 1 < len(lines) and lines[end_idx + 1].strip() == "":
        end_idx += 1

    new_lines = lines[:start_idx] + lines[end_idx + 1:]

    with open(filepath, "w") as f:
        f.writelines(new_lines)

    return True


def append_icon_to_file(filepath: str, style: str, icon_code: str, description: str = "Custom"):
    """Append an icon code block to a file, creating it if needed."""
    if not os.path.exists(filepath):
        # Create new file with appropriate header
        if style == "outline":
            header = OUTLINE_HEADER.format(description=description)
        else:
            header = FILLED_HEADER.format(description=description)
        with open(filepath, "w") as f:
            f.write(header)
            f.write("\n")
            f.write(icon_code)
            f.write("\n")
        return

    # Append to existing file
    with open(filepath) as f:
        content = f.read()

    # Ensure trailing newline then add icon block
    if not content.endswith("\n"):
        content += "\n"

    content += "\n" + icon_code + "\n"

    with open(filepath, "w") as f:
        f.write(content)


def scan_all_icons() -> list[dict]:
    """Scan all icon files and return a list of icon info dicts."""
    icons = []
    for style, icon_dir, style_obj in [
        ("outline", OUTLINE_DIR, "Outline"),
        ("filled", FILLED_DIR, "Filled"),
    ]:
        if not os.path.isdir(icon_dir):
            continue
        for fname in sorted(os.listdir(icon_dir)):
            if not fname.endswith(".kt"):
                continue
            fpath = os.path.join(icon_dir, fname)
            with open(fpath) as f:
                content = f.read()
            # Find all icon property names in the file
            pattern = rf"val TablerIcons\.{style_obj}\.(\w+): ImageVector"
            for match in re.finditer(pattern, content):
                icons.append({
                    "name": match.group(1),
                    "style": style,
                    "file": fname,
                    "filepath": fpath,
                })
    return icons


# ─── Commands ─────────────────────────────────────────────────────────────────


def cmd_add(args):
    """Add a new Tabler icon."""
    style = args.style
    tabler_name = args.tabler_name
    kotlin_name = args.kotlin_name
    target_file = resolve_target_file(style, args.file)

    print(f"\n📥 Adding {style} icon: {tabler_name} → TablerIcons."
          f"{'Outline' if style == 'outline' else 'Filled'}.{kotlin_name}")

    # Check for duplicates across all files
    existing = find_icon_file(style, kotlin_name)
    if existing:
        rel = os.path.relpath(existing, PROJECT_ROOT)
        print(f"  ✗ Icon '{kotlin_name}' already exists in {rel}")
        print(f"    Use 'update' to re-download, or 'remove' first.")
        sys.exit(1)

    # Download SVG
    svg_content = download_svg(tabler_name, style)
    paths = extract_paths(svg_content)
    if not paths:
        print(f"  ✗ No valid paths found in SVG for '{tabler_name}'.")
        sys.exit(1)
    print(f"  Found {len(paths)} path(s)")

    # Generate Kotlin code
    if style == "outline":
        code = generate_outline_icon(kotlin_name, paths)
    else:
        code = generate_filled_icon(kotlin_name, paths)

    # Check line limit before writing
    target_rel = os.path.relpath(target_file, PROJECT_ROOT)
    new_icon_lines = code.count("\n") + 1
    current_lines = file_line_count(target_file) if os.path.exists(target_file) else 18
    projected = current_lines + new_icon_lines + 1  # +1 for separator blank line
    if projected > MAX_LINES:
        print(f"  ⚠ Adding this icon would push {target_rel} to ~{projected} lines "
              f"(limit: {MAX_LINES}).")
        print(f"    Use --file to target a different file, or create a new group file.")
        sys.exit(1)

    # Append to file
    description = os.path.splitext(os.path.basename(target_file))[0]
    append_icon_to_file(target_file, style, code, description)

    final_lines = file_line_count(target_file)
    print(f"  ✓ Added to {target_rel} ({final_lines} lines)")
    print(f"\n💡 Run: ./gradlew ktlintFormat to fix import ordering if needed.")


def cmd_remove(args):
    """Remove an existing Tabler icon."""
    style = args.style
    kotlin_name = args.kotlin_name

    print(f"\n🗑  Removing {style} icon: {kotlin_name}")

    # Find which file contains the icon
    filepath = find_icon_file(style, kotlin_name)
    if filepath is None:
        print(f"  ✗ Icon '{kotlin_name}' not found in any {style} icon file.")
        sys.exit(1)

    rel = os.path.relpath(filepath, PROJECT_ROOT)
    removed = remove_icon_block(filepath, kotlin_name, style)
    if removed:
        remaining = file_line_count(filepath)
        print(f"  ✓ Removed from {rel} ({remaining} lines remaining)")
        print(f"\n⚠  Remember to remove any imports of this icon from consuming files:")
        sub_pkg = "outline" if style == "outline" else "filled"
        print(f"    import {BASE_PKG}.{sub_pkg}.{kotlin_name}")
        print(f"\n💡 Tip: grep for '{kotlin_name}' across the codebase:")
        print(f"    grep -rn '{kotlin_name}' --include='*.kt' | grep -v build/")
    else:
        print(f"  ✗ Failed to remove icon block from {rel}")
        sys.exit(1)


def cmd_update(args):
    """Re-download SVG and regenerate an existing icon."""
    style = args.style
    tabler_name = args.tabler_name
    kotlin_name = args.kotlin_name

    print(f"\n🔄 Updating {style} icon: {tabler_name} → {kotlin_name}")

    # Find existing file
    filepath = find_icon_file(style, kotlin_name)
    if filepath is None:
        print(f"  ✗ Icon '{kotlin_name}' not found in any {style} icon file.")
        print(f"    Use 'add' to create it first.")
        sys.exit(1)

    rel = os.path.relpath(filepath, PROJECT_ROOT)
    print(f"  Found in {rel}")

    # Download fresh SVG
    svg_content = download_svg(tabler_name, style)
    paths = extract_paths(svg_content)
    if not paths:
        print(f"  ✗ No valid paths found in SVG for '{tabler_name}'.")
        sys.exit(1)
    print(f"  Found {len(paths)} path(s)")

    # Generate new code
    if style == "outline":
        new_code = generate_outline_icon(kotlin_name, paths)
    else:
        new_code = generate_filled_icon(kotlin_name, paths)

    # Remove old block and insert new one
    remove_icon_block(filepath, kotlin_name, style)
    append_icon_to_file(filepath, style, new_code)

    final_lines = file_line_count(filepath)
    print(f"  ✓ Updated in {rel} ({final_lines} lines)")
    print(f"\n💡 Run: ./gradlew ktlintFormat to fix import ordering if needed.")


def cmd_list(args):
    """List all registered icons."""
    style_filter = getattr(args, "style", None)
    icons = scan_all_icons()

    if style_filter:
        icons = [i for i in icons if i["style"] == style_filter]

    if not icons:
        print("\nNo icons found.")
        return

    # Group by style and file
    outline_icons = [i for i in icons if i["style"] == "outline"]
    filled_icons = [i for i in icons if i["style"] == "filled"]

    print(f"\n📋 Registered Tabler Icons ({len(icons)} total)")

    if outline_icons and (not style_filter or style_filter == "outline"):
        print(f"\n  Outline ({len(outline_icons)} icons):")
        by_file: dict[str, list] = {}
        for icon in outline_icons:
            by_file.setdefault(icon["file"], []).append(icon["name"])
        for fname, names in sorted(by_file.items()):
            fpath = os.path.join(OUTLINE_DIR, fname)
            lines = file_line_count(fpath)
            capacity = MAX_LINES - lines
            print(f"    {fname} ({lines} lines, ~{capacity} lines free):")
            for name in names:
                print(f"      • {name}")

    if filled_icons and (not style_filter or style_filter == "filled"):
        print(f"\n  Filled ({len(filled_icons)} icons):")
        by_file = {}
        for icon in filled_icons:
            by_file.setdefault(icon["file"], []).append(icon["name"])
        for fname, names in sorted(by_file.items()):
            fpath = os.path.join(FILLED_DIR, fname)
            lines = file_line_count(fpath)
            capacity = MAX_LINES - lines
            print(f"    {fname} ({lines} lines, ~{capacity} lines free):")
            for name in names:
                print(f"      • {name}")


def cmd_check(args):
    """Check all icon files against the 600-line Konsist limit."""
    print(f"\n🔍 Checking icon files against {MAX_LINES}-line limit…\n")
    all_ok = True

    for style_dir, label in [(OUTLINE_DIR, "outline"), (FILLED_DIR, "filled")]:
        if not os.path.isdir(style_dir):
            continue
        for fname in sorted(os.listdir(style_dir)):
            if not fname.endswith(".kt"):
                continue
            fpath = os.path.join(style_dir, fname)
            lines = file_line_count(fpath)
            rel = os.path.relpath(fpath, PROJECT_ROOT)
            if lines > MAX_LINES:
                print(f"  ✗ {rel}: {lines} lines (OVER LIMIT by {lines - MAX_LINES})")
                all_ok = False
            elif lines > MAX_LINES - 50:
                print(f"  ⚠ {rel}: {lines} lines (approaching limit)")
            else:
                print(f"  ✓ {rel}: {lines} lines")

    if all_ok:
        print("\n✅ All icon files within limits.")
    else:
        print(f"\n❌ Some files exceed the {MAX_LINES}-line limit!")
        print("   Split icons into a new group file (e.g., ActionIcons2.kt).")
        sys.exit(1)


# ─── CLI ──────────────────────────────────────────────────────────────────────


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Tabler Icons maintenance tool for SplitTrip.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=textwrap.dedent("""\
            Examples:
              %(prog)s add outline heart Heart
              %(prog)s add outline heart Heart --file ContentIcons
              %(prog)s add filled heart HeartFilled
              %(prog)s remove outline Heart
              %(prog)s update outline heart Heart
              %(prog)s list
              %(prog)s list --style filled
              %(prog)s check
        """),
    )
    sub = parser.add_subparsers(dest="command", required=True)

    # ── add ──
    p_add = sub.add_parser("add", help="Add a new icon")
    p_add.add_argument("style", choices=["outline", "filled"],
                        help="Icon style (outline or filled)")
    p_add.add_argument("tabler_name",
                        help="Tabler icon slug (e.g. 'arrow-left', 'heart')")
    p_add.add_argument("kotlin_name",
                        help="Kotlin property name (e.g. 'ArrowLeft', 'Heart')")
    p_add.add_argument("--file", default=None,
                        help="Target group file without .kt (default: MiscIcons / FilledIcons)")
    p_add.set_defaults(func=cmd_add)

    # ── remove ──
    p_rm = sub.add_parser("remove", help="Remove an existing icon")
    p_rm.add_argument("style", choices=["outline", "filled"],
                       help="Icon style (outline or filled)")
    p_rm.add_argument("kotlin_name",
                       help="Kotlin property name to remove (e.g. 'Heart')")
    p_rm.set_defaults(func=cmd_remove)

    # ── update ──
    p_up = sub.add_parser("update", help="Re-download and regenerate an icon")
    p_up.add_argument("style", choices=["outline", "filled"],
                       help="Icon style (outline or filled)")
    p_up.add_argument("tabler_name",
                       help="Tabler icon slug (e.g. 'arrow-left')")
    p_up.add_argument("kotlin_name",
                       help="Kotlin property name (e.g. 'ArrowLeft')")
    p_up.set_defaults(func=cmd_update)

    # ── list ──
    p_ls = sub.add_parser("list", help="List all registered icons")
    p_ls.add_argument("--style", choices=["outline", "filled"], default=None,
                       help="Filter by style")
    p_ls.set_defaults(func=cmd_list)

    # ── check ──
    p_ck = sub.add_parser("check", help="Check icon files against 600-line limit")
    p_ck.set_defaults(func=cmd_check)

    return parser


def main():
    parser = build_parser()
    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()

