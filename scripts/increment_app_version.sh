#!/bin/bash

file_name="app/build.gradle"

# Function to increment version number in a string
increment_version_number() {
  local content="$1"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E '^val\s+versionCode\s*=\s*[0-9]+$' <<< "$content")

  # Extract current version number
  current_version=${version_line##*=}

  # Increment version number
  new_version=$((current_version + 1))

  # Replace old version number with new one
  new_content=$(echo "$content" | sed "s/$version_line/val versionNumber = $new_version/")

  echo "$new_content"
}

increment_version_code() {
  local content="$1"
  local new_code="$2"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E '^val\s+versionName\s*=\s*"([0-9].+)"+$' <<< "$content")

  # Replace old version number with new one
  new_content=$(echo "$content" | sed "s/$version_line/val version = \"$new_code\"/")

  echo "$new_content"
}

new_version_code="$1"
content=$(cat $file_name)
updated_file_string=""
new_content=""

# Replace old version number with new one
echo "updating version number"
updated_file_string=$(increment_version_number "$content")
echo "updating version name"
new_content=$(increment_version_code "$updated_file_string" "$new_version_code")

echo "$new_content" > "$file_name"
