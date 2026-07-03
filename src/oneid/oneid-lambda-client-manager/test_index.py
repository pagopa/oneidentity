"""
Unit tests for index.py
"""
import unittest
from unittest.mock import patch, Mock, MagicMock
from index import check_client_id_exists, get_cognito_client, get_dynamodb_client, app


class TestCheckClientIdExists(unittest.TestCase):
    """Tests for the check_client_id_exists function."""
    
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_item_found(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.return_value = {"Item": {"clientId": {"S": "test_client_id"}}}
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertTrue(result)

    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_item_not_found(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.return_value = {}
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertFalse(result)

    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_dynamodb_exception(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.side_effect = Exception("DynamoDB error")
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertFalse(result)


class TestGetCognitoClient(unittest.TestCase):
    """Tests for the get_cognito_client function."""
    
    @patch("index.boto3.client")
    def test_client_creation_success(self, mock_boto3_client):
        mock_boto3_client.return_value = Mock()
        result = get_cognito_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("cognito-idp", region_name="eu-south-1")
        self.assertIsNotNone(result)

    @patch("index.boto3.client")
    def test_client_creation_failure(self, mock_boto3_client):
        mock_boto3_client.side_effect = Exception("Cognito client error")
        result = get_cognito_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("cognito-idp", region_name="eu-south-1")
        self.assertIsNone(result)


class TestGetDynamoDBClient(unittest.TestCase):
    """Tests for the get_dynamodb_client function."""
    
    @patch("index.boto3.client")
    def test_client_creation_success(self, mock_boto3_client):
        mock_boto3_client.return_value = Mock()
        result = get_dynamodb_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("dynamodb", region_name="eu-south-1")
        self.assertIsNotNone(result)

    @patch("index.boto3.client")
    def test_client_creation_failure(self, mock_boto3_client):
        mock_boto3_client.side_effect = Exception("DynamoDB client error")
        result = get_dynamodb_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("dynamodb", region_name="eu-south-1")
        self.assertIsNone(result)


class TestCreateIdpInternalUserAge(unittest.TestCase):
    """Tests for age field in create_idp_internal_user."""

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_create_user_with_age(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.put_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}

        event = {
            "httpMethod": "POST",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"username":"testuser","password":"pass","samlAttributes":{"name":"Mario"},"age":16}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 201)
        call_args = mock_dynamodb.put_item.call_args
        item = call_args[1]["Item"] if "Item" in call_args[1] else call_args.kwargs["Item"]
        self.assertEqual(item["age"], {"N": "16"})

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_create_user_without_age(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.put_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}

        event = {
            "httpMethod": "POST",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"username":"testuser","password":"pass","samlAttributes":{"name":"Mario"}}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 201)
        call_args = mock_dynamodb.put_item.call_args
        item = call_args[1]["Item"] if "Item" in call_args[1] else call_args.kwargs["Item"]
        self.assertNotIn("age", item)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_create_user_invalid_username_returns_400(self, mock_getenv, mock_dynamodb,
                                                      mock_get_user, mock_extract):
        event = {
            "httpMethod": "POST",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"username":"javascript:alert","password":"pass","samlAttributes":{"name":"Mario"}}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_create_user_dangerous_password_returns_400(self, mock_getenv, mock_dynamodb,
                                                        mock_get_user, mock_extract):
        event = {
            "httpMethod": "POST",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"username":"testuser","password":"javascript:alert","samlAttributes":{"name":"Mario"}}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_create_user_blank_password_returns_400(self, mock_getenv, mock_dynamodb,
                                                    mock_get_user, mock_extract):
        event = {
            "httpMethod": "POST",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"username":"testuser","password":"   ","samlAttributes":{"name":"Mario"}}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)


class TestUpdateIdpInternalUserAge(unittest.TestCase):
    """Tests for age field in update_idp_internal_user."""

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_with_age_only(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.update_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}

        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"samlAttributes":{"name":"Mario"},"age":18}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 200)
        call_args = mock_dynamodb.update_item.call_args
        kwargs = call_args[1] if call_args[1] else call_args.kwargs
        self.assertIn("age = :age", kwargs["UpdateExpression"])
        self.assertEqual(kwargs["ExpressionAttributeValues"][":age"], {"N": "18"})

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_with_age_and_saml(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.update_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}

        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"samlAttributes":{"name":"Luigi"},"age":20}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 200)
        call_args = mock_dynamodb.update_item.call_args
        kwargs = call_args[1] if call_args[1] else call_args.kwargs
        self.assertIn("samlAttributes = :samlAttributes", kwargs["UpdateExpression"])
        self.assertIn("age = :age", kwargs["UpdateExpression"])

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_with_password_only(self, mock_getenv, mock_dynamodb, mock_get_user,
                                            mock_extract):
        mock_dynamodb.update_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}

        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"password":"new-password"}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 200)
        call_args = mock_dynamodb.update_item.call_args
        kwargs = call_args[1] if call_args[1] else call_args.kwargs
        self.assertIn("password = :password", kwargs["UpdateExpression"])
        self.assertEqual(kwargs["ExpressionAttributeValues"][":password"], {"S": "new-password"})

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_invalid_username_returns_400(self, mock_getenv, mock_dynamodb,
                                                      mock_get_user, mock_extract):
        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/javascript:alert",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"password":"new-password"}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "javascript:alert"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_dangerous_password_returns_400(self, mock_getenv, mock_dynamodb,
                                                        mock_get_user, mock_extract):
        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"password":"javascript:alert"}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_blank_password_returns_400(self, mock_getenv, mock_dynamodb,
                                                    mock_get_user, mock_extract):
        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{"password":"   "}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_update_user_no_saml_no_age_returns_400(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        event = {
            "httpMethod": "PATCH",
            "path": "/client-manager/client-users/testuser",
            "headers": {"Authorization": "Bearer dummy"},
            "body": '{}',
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users/{username}",
            "pathParameters": {"username": "testuser"},
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 400)


class TestGetIdpInternalUsersAge(unittest.TestCase):
    """Tests for age field in get_idp_internal_users."""

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_get_users_includes_age(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.query.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200},
            "Items": [
                {
                    "username": {"S": "testuser"},
                    "password": {"S": "pass"},
                    "samlAttributes": {"M": {"name": {"S": "Mario"}}},
                    "age": {"N": "16"},
                }
            ],
        }

        event = {
            "httpMethod": "GET",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "queryStringParameters": {"limit": "10"},
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 200)
        import json
        body = json.loads(response["body"])
        self.assertEqual(body["users"][0]["age"], 16)

    @patch("index.extract_client_id_from_connected_user", return_value="client-123")
    @patch("index.get_user_id_from_bearer", return_value="user-123")
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_get_users_without_age(self, mock_getenv, mock_dynamodb, mock_get_user, mock_extract):
        mock_dynamodb.query.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200},
            "Items": [
                {
                    "username": {"S": "testuser"},
                    "password": {"S": "pass"},
                    "samlAttributes": {"M": {"name": {"S": "Mario"}}},
                }
            ],
        }

        event = {
            "httpMethod": "GET",
            "path": "/client-manager/client-users",
            "headers": {"Authorization": "Bearer dummy"},
            "queryStringParameters": {"limit": "10"},
            "requestContext": {"stage": "test"},
            "resource": "/client-manager/client-users",
        }

        response = app.resolve(event, {})
        self.assertEqual(response["statusCode"], 200)
        import json
        body = json.loads(response["body"])
        self.assertNotIn("age", body["users"][0])


if __name__ == "__main__":
    unittest.main()
