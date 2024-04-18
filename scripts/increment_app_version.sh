#!/bin/bash

file_name="$1"
new_version_name="$2"
new_version_code="$3"

# Function to increment version number in a string
increment_version_number() {
  local content="$1"
  local version_code="$2"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E 'versionCode\s+[0-9]+$' <<< "$content")

  # Extract current version number
  current_version=3

  # Increment version number
  new_version_code=""

  if [ -z "$version_code" ]; then
    new_version_code=$((current_version + 1))
  else
    new_version_code=$version_code
  fi

  # Replace old version number with new one
  new_content=${content//$version_line/        versionCode $new_version_code}

  echo "$new_content"
}

increment_version_code() {
  local content="$1"
  local new_code="$2"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E 'versionName\s+"([0-9].+)"+$' <<< "$content")

  # Replace old version number with new one
  new_content=${content//$version_line/        versionName \"$new_code\"}

  echo "$new_content"
}

content=$(cat "$file_name")
updated_file_string=""
new_content=""

# Replace old version number with new one
echo "updating version number $new_version_code"
updated_file_string=$(increment_version_number "$content" "$new_version_code")
echo "updating version name"
new_content=$(increment_version_code "$updated_file_string" "$new_version_name")

# Check if the version number was updated
if [ -z "$new_content" ]; then
  echo "Failed to update version number"
  exit 1
fi

echo "$new_content" > "$file_name"
echo "Version number updated successfully"