"""
Unit tests for index.py
"""
import unittest
from unittest.mock import patch, Mock
from index import check_client_id_exists, get_cognito_client, get_dynamodb_client


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


if __name__ == "__main__":
    unittest.main()