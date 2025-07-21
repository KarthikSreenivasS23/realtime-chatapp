# ChatApp API Testing Guide

This document provides a step-by-step guide to test the ChatApp API, including authentication with Keycloak and interacting with the core endpoints.

## Prerequisites

1.  **Docker is running.**
2.  The application is running via `docker-compose up --build`.
3.  You have a REST client like [Postman](https://www.postman.com/) installed.

## Step 1: Obtain an Authentication Token from Keycloak

Before you can test the application's endpoints, you need an access token for the `testuser`.

1.  **Endpoint:** `POST` `http://localhost:9090/realms/chatspot-chatapp/protocol/openid-connect/token`
2.  **Headers:**
    *   `Content-Type`: `application/x-www-form-urlencoded`
3.  **Body (x-www-form-urlencoded):**
    *   `client_id`: `chatspot-backend-client`
    *   `username`: `testuser`
    *   `password`: `password`
    *   `grant_type`: `password`
    *   `scope`: `openid`

**Expected Response (200 OK):**

You will receive a JSON response containing an `access_token`. Copy this token (without the quotes).

```json
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIg...",
    "expires_in": 300,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIg...",
    "token_type": "bearer",
    "not-before-policy": 0,
    "session_state": "...",
    "scope": "openid email profile"
}
```

**Save the `access_token` for the next steps.**

## Step 2: Test API Endpoints

For all subsequent requests, you must include the `access_token` as a Bearer Token in the `Authorization` header.

*   **Header:** `Authorization`
*   **Value:** `Bearer <YOUR_ACCESS_TOKEN>`

### 2.1 Get Current User Profile

*   **Endpoint:** `GET` `http://localhost:8080/api/users/me`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_ACCESS_TOKEN>`
*   **Expected Response (200 OK):**
    A JSON object with the profile of `testuser`.

### 2.2 Get All Users

*   **Endpoint:** `GET` `http://localhost:8080/api/users`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_ACCESS_TOKEN>`
*   **Expected Response (200 OK):**
    A JSON array of all users in the system.

### 2.3 Send a Message

*   **Endpoint:** `POST` `http://localhost:8080/api/messages`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_ACCESS_TOKEN>`
    *   `Content-Type`: `application/json`
*   **Body (raw JSON):**

    ```json
    {
        "recipientId": "<USER_ID_OF_RECIPIENT>",
        "content": "Hello, this is a test message!",
        "messageType": "TEXT"
    }
    ```

    > **Note:** Get the `USER_ID_OF_RECIPIENT` from the response of the "Get All Users" endpoint.

*   **Expected Response (200 OK):**
    A JSON object representing the newly created message.

### 2.4 Get Conversation History

*   **Endpoint:** `GET` `http://localhost:8080/api/messages/{userId1}/{userId2}`
*   **Headers:**
    *   `Authorization`: `Bearer <YOUR_ACCESS_TOKEN>`
*   **URL Parameters:**
    *   `userId1`: ID of the first user (e.g., your `testuser` ID).
    *   `userId2`: ID of the second user in the conversation.
*   **Expected Response (200 OK):**
    A JSON array of all messages exchanged between the two specified users.

## Common Errors

*   **401 Unauthorized:** This means your `access_token` is missing, invalid, or expired. Re-run Step 1 to get a new token.
*   **404 Not Found:** Check that the endpoint URL is correct and that the resource you are requesting (e.g., a user ID) exists.
*   **500 Internal Server Error:** This indicates a problem on the server side. Check the `chatapp` container logs using `docker-compose logs -f chatapp` for error details.
