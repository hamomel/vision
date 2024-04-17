#!/bin/bash

file_name="app/build.gradle"

# Function to increment version number in a string
increment_version_number() {
  local content="$1"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E 'versionCode\s+[0-9]+$' <<< "$content")

  # Extract current version number
  current_version=1

  # Increment version number
  new_version=$((current_version + 1))

  # Replace old version number with new one
  new_content=$(echo "$content" | sed "s/$version_line/        versionCode $new_version/")

  echo "$new_content"
}

increment_version_code() {
  local content="$1"
  local new_code="$2"
  local new_content=""

  # Find the line containing the version number
  version_line=$(grep -E 'versionName\s+"([0-9].+)"+$' <<< "$content")

  # Replace old version number with new one
  new_content=$(echo "$content" | sed "s/$version_line/        versionName \"$new_code\"/")

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

# Check if the version number was updated
if [ -z "$new_content" ]; then
  echo "Failed to update version number"
  exit 1
fi

echo "$new_content" > "$file_name"
echo "Version number updated successfully"