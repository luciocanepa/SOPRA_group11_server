import requests
import sys

from utils import create_user, create_group, invite_user_to_group, accept_invitation, add_user_to_group

def basic_setup():
    """
    This function creates two users and a group, and adds the second user to the group.
    """
    print("Creating first user...")
    user1 = create_user("user1", "password1")
    print(f"Created user1 with ID: {user1['id']}, Token: {user1['token']}")
    user1_token = user1['token']

    # Create second user
    print("\nCreating second user...")
    user2 = create_user("user2", "password2")
    print(f"Created user2 with ID: {user2['id']}, Token: {user2['token']}")
    user2_token = user2['token']

    # Create a group with user1
    print("\nCreating group with user1...")
    group = create_group("Test Group", user1_token)
    print(f"Created group: ID={group['id']}, Name={group['name']}, Admin ID={group['adminId']}")

    # Add user2 to the group using the helper function
    print("\nAdding user2 to the group...")
    accepted_group = add_user_to_group(group['id'], user2['id'], user1_token, user2_token)
    print(f"User2 successfully joined the group: {accepted_group['name']}")

def advanced_setup():
    """
    This function creates 10 users and 4 groups with specified memberships.
    user1 is admin of group1
    user1 is admin for group4
    user2 is admin of group 2
    user3 is admin of group3

    group1 has user2, user4, user5, user6 and user7 as members
    group2 has  user6, user7, user8, user9 and user10 as members
    group 3 has user1, user2, user5, user7, user9 as members
    """
    # Create 10 users
    users = []
    user_tokens = []
    print("Creating 10 users...")
    for i in range(1, 11):
        user = create_user(f"user{i}", f"password{i}")
        users.append(user)
        user_tokens.append(user['token'])
        print(f"Created user{i} with ID: {user['id']}, Token: {user['token']}")

    # Create 4 groups with specified admins
    print("\nCreating groups...")
    group1 = create_group("Group 1", user_tokens[0])  # user1 is admin
    print(f"Created group1: ID={group1['id']}, Admin=user1")
    
    group2 = create_group("Group 2", user_tokens[1])  # user2 is admin
    print(f"Created group2: ID={group2['id']}, Admin=user2")
    
    group3 = create_group("Group 3", user_tokens[2])  # user3 is admin
    print(f"Created group3: ID={group3['id']}, Admin=user3")
    
    group4 = create_group("Group 4", user_tokens[0])  # user1 is admin
    print(f"Created group4: ID={group4['id']}, Admin=user1")

    # Add members to group1 (user2, user4, user5, user6, user7)
    print("\nAdding members to group1...")
    members_group1 = [1, 3, 4, 5, 6]  # indices for user2, user4, user5, user6, user7
    for member_idx in members_group1:
        add_user_to_group(group1['id'], users[member_idx]['id'], user_tokens[0], user_tokens[member_idx])
        print(f"Added user{member_idx + 1} to group1")

    # Add members to group2 (user6, user7, user8, user9, user10)
    print("\nAdding members to group2...")
    members_group2 = [5, 6, 7, 8, 9]  # indices for user6, user7, user8, user9, user10
    for member_idx in members_group2:
        add_user_to_group(group2['id'], users[member_idx]['id'], user_tokens[1], user_tokens[member_idx])
        print(f"Added user{member_idx + 1} to group2")

    # Add members to group3 (user1, user2, user5, user7, user9)
    print("\nAdding members to group3...")
    members_group3 = [0, 1, 4, 6, 8]  # indices for user1, user2, user5, user7, user9
    for member_idx in members_group3:
        add_user_to_group(group3['id'], users[member_idx]['id'], user_tokens[2], user_tokens[member_idx])
        print(f"Added user{member_idx + 1} to group3")

def main():
    try:        
        if len(sys.argv) != 2 or sys.argv[1] not in ["basic", "advanced"]:
            print("Usage: python test_api.py [basic|advanced]")
            sys.exit(1)
            
        if sys.argv[1] == "basic":
            basic_setup()
        else:
            advanced_setup()

        print("\nAll operations completed successfully!")

    except requests.exceptions.RequestException as e:
        print(f"Error occurred: {e}")
        if hasattr(e.response, 'text'):
            print(f"Response: {e.response.text}")

if __name__ == "__main__":
    main()