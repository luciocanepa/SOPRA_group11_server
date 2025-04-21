import requests
import json

# API base URL
BASE_URL = "http://localhost:8080"

def create_user(username, password):
    """Create a new user"""
    url = f"{BASE_URL}/users/register"
    data = {
        "username": username,
        "password": password
    }
    response = requests.post(url, json=data)
    response.raise_for_status()
    return response.json()

def create_group(name, token):
    """Create a new group"""
    url = f"{BASE_URL}/groups"
    headers = {"Authorization": token}
    data = {"name": name}
    response = requests.post(url, json=data, headers=headers)
    response.raise_for_status()
    return response.json()

def invite_user_to_group(group_id, invitee_id, token):
    """Invite a user to join a group"""
    url = f"{BASE_URL}/groups/{group_id}/invitations"
    headers = {"Authorization": token}
    data = {"inviteeId": invitee_id}
    response = requests.post(url, json=data, headers=headers)
    response.raise_for_status()
    return response.json()

def accept_invitation(invitation_id, token):
    """Accept a group invitation"""
    url = f"{BASE_URL}/invitations/{invitation_id}/accept"
    headers = {"Authorization": token}
    response = requests.put(url, headers=headers)
    response.raise_for_status()
    return response.json()

def add_user_to_group(group_id, invitee_id, admin_token, invitee_token):
    """Invite and add a user to a group in one step"""
    # First invite the user
    invitation = invite_user_to_group(group_id, invitee_id, admin_token)
    # Then accept the invitation
    return accept_invitation(invitation['id'], invitee_token)