# ChatApp API Testing with Postman and Keycloak

This guide provides step-by-step instructions to configure Keycloak and Postman to test the ChatApp backend API.

## Prerequisites

- The application stack must be running via `docker-compose up`.
- You have Postman installed.

---

## Step 1: Configure Keycloak

First, we need to create a dedicated client for Postman and a test user within your Keycloak realm.

### A. Create a New Client for Postman

1.  **Open Keycloak**: Navigate to the Keycloak Admin Console at [`http://localhost:9090`](http://localhost:9090).
2.  **Login**: Use the credentials `admin` / `password`.
3.  **Select Realm**: From the realm dropdown in the top-left, make sure you select `chatspot-chatapp`.
4.  **Go to Clients**: In the left-hand menu, click on `Clients`.
5.  **Create Client**:
    *   Click the `Create client` button on the right.
    *   **Client ID**: Enter `postman-client`.
    *   Click `Next`.
6.  **Configure Client Settings**:
    *   Turn on `Client authentication`.
    *   Ensure `Standard flow` and `Direct access grants` are checked.
    *   Click `Next`.
7.  **Save the Client**: Click `Save`.

### B. Create a Test User

1.  **Go to Users**: In the left-hand menu, click `Users`.
2.  **Create User**:
    *   Click `Create new user`.
    *   **Username**: Enter `testuser`.
    *   Click `Create`.
3.  **Set Password**:
    *   After the user is created, go to the `Credentials` tab.
    *   Click `Set password`.
    *   **Password**: Enter a password (e.g., `password`).
    *   **Temporary**: Turn this OFF.
    *   Click `Save`, and confirm by clicking `Save password`.

---

## Step 2: Import and Configure Postman

### A. Import the Collection

1.  Open Postman.
2.  Go to `File > Import...` and select the `ChatApp.postman_collection.json` file located in this directory.

### B. Get an Access Token

1.  In the imported collection, open the `Authentication` folder.
2.  Select the `Get Access Token` request.
3.  Go to the `Body` tab. The request is pre-configured to use the `password` grant type with the `testuser` we created.
4.  Click **Send**. You should see a JSON response containing an `access_token`.

> This request has a test script that automatically saves the `access_token` to a collection variable. All other requests in this collection are pre-configured to use this token for authentication.

---

## Step 3: Test the API

You can now run the other requests in the collection:

-   **Users**: Create new users, get user details, etc.
-   **Messages**: Send messages and check different API functionalities.

All requests will automatically use the Bearer Token obtained in the previous step. If your token expires, simply run the `Get Access Token` request again.


# Optional: Refresh Token
Use the `refresh_token` in your auth response to request a new access token without re-authenticating.
