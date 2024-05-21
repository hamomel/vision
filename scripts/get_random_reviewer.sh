#!/bin/bash

not_reviewers=("user1" "user2")

users=("user1" "user2" "user3" "user4" "user5")

filtered_users=()
for user in "${users[@]}"; do
  if [[ ! " ${not_reviewers[@]} " =~ " ${user} " ]]; then
    filtered_users+=("$user")
  fi
done

random_index=$((RANDOM % ${#filtered_users[@]}))
random_user=${filtered_users[$random_index]}

echo "$random_user"
