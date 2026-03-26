#!/bin/bash

# --- Configuration & Constants ---
LINE_THRESHOLD=400
FILE_EXTENSION="kt"

# Resolve the script's directory and its parent (Project Root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Target directory: User argument OR the parent of /scripts
TARGET_DIRECTORY="${1:-$DEFAULT_PROJECT_ROOT}"

# --- Functions ---

validate_directory() {
    local dir="$1"
    if [[ ! -d "$dir" ]]; then
        echo "Error: Directory '$dir' does not exist." >&2
        exit 1
    fi
}

# Function to collect data and return a raw list "lines path"
# This separates logic from presentation
collect_file_data() {
    local search_path="$1"
    local ext="$2"

    find "$search_path" -type f -name "*.$ext" \
        -not -path '*/.*' \
        -not -path '*/build/*' \
        -not -path '*/target/*' | while read -r file; do

        local line_count
        line_count=$(wc -l < "$file" | xargs)

        if [[ "$line_count" -gt "$LINE_THRESHOLD" ]]; then
            # Output format: "count path" for easy sorting
            echo "$line_count $file"
        fi
    done
}

# Function to handle the final output formatting
format_output() {
    while read -r count path; do
        printf "Lines: %-5s | Path: %s\n" "$count" "$path"
    done
}

# --- Main Execution ---

main() {
    validate_directory "$TARGET_DIRECTORY"

    echo "Project Root: $TARGET_DIRECTORY"
    echo "Searching *.$FILE_EXTENSION files with > $LINE_THRESHOLD lines (Sorted by size)..."
    echo "--------------------------------------------------------------------------------"

    # 1. Collect data
    # 2. Sort: -n (numeric), -r (reverse, largest first)
    # 3. Format: Pretty print the result
    collect_file_data "$TARGET_DIRECTORY" "$FILE_EXTENSION" | sort -nr | format_output

    echo "--------------------------------------------------------------------------------"
    echo "Analysis finished."
}

main
