import datetime
import os
import unittest
from unittest.mock import MagicMock, patch

import pytest
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID
from freezegun import freeze_time

FROZEN_TIME = "2026-05-12 12:00:00"


def generate_cert_pem(days_until_expiry: int) -> str:
    """Generate a self-signed certificate expiring in `days_until_expiry` days from FROZEN_TIME."""
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    now = datetime.datetime(2026, 5, 12, 12, 0, 0, tzinfo=datetime.timezone.utc)
    cert = (
        x509.CertificateBuilder()
        .subject_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "test")]))
        .issuer_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, "test")]))
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - datetime.timedelta(days=1))
        .not_valid_after(now + datetime.timedelta(days=days_until_expiry))
        .sign(key, hashes.SHA256())
    )
    return cert.public_bytes(serialization.Encoding.PEM).decode()


MOCK_PARAM_NAME = "test-cert-param"
MOCK_SNS_TOPIC = "arn:aws:sns:eu-south-1:123456789012:test-topic"

ENV_PATCH = {
    "PARAM_NAME": MOCK_PARAM_NAME,
    "SNS_TOPIC": MOCK_SNS_TOPIC,
}


def make_ssm_response(cert_pem: str) -> dict:
    return {"Parameter": {"Name": MOCK_PARAM_NAME, "Value": cert_pem}}


class TestLambdaHandler(unittest.TestCase):

    @freeze_time(FROZEN_TIME)
    def _run_handler(self, cert_pem: str):
        """Helper: patches boto3 clients and runs lambda_handler with frozen time."""
        mock_ssm = MagicMock()
        mock_ssm.get_parameter.return_value = make_ssm_response(cert_pem)
        mock_sns = MagicMock()

        boto3_client_map = {"ssm": mock_ssm, "sns": mock_sns}

        with patch.dict(os.environ, ENV_PATCH):
            with patch("boto3.client", side_effect=lambda svc: boto3_client_map[svc]):
                import importlib
                import index
                importlib.reload(index)  # reload so module-level env vars are picked up
                index.lambda_handler({}, None)

        return mock_ssm, mock_sns

    def test_cert_expiring_soon_sends_sns(self):
        """Certificate expiring in 10 days → SNS publish should be called."""
        cert_pem = generate_cert_pem(days_until_expiry=10)
        _, mock_sns = self._run_handler(cert_pem)
        mock_sns.publish.assert_called_once()
        call_kwargs = mock_sns.publish.call_args[1]
        self.assertEqual(call_kwargs["TopicArn"], MOCK_SNS_TOPIC)
        self.assertIn("10", call_kwargs["Message"])

    def test_cert_expiring_exactly_15_days_sends_sns(self):
        """Certificate expiring in exactly 15 days → SNS publish should be called."""
        cert_pem = generate_cert_pem(days_until_expiry=15)
        _, mock_sns = self._run_handler(cert_pem)
        mock_sns.publish.assert_called_once()

    def test_cert_expiring_in_16_days_no_sns(self):
        """Certificate expiring in 16 days → SNS publish should NOT be called."""
        cert_pem = generate_cert_pem(days_until_expiry=16)
        _, mock_sns = self._run_handler(cert_pem)
        mock_sns.publish.assert_not_called()

    def test_cert_valid_long_term_no_sns(self):
        """Certificate with long validity → SNS publish should NOT be called."""
        cert_pem = generate_cert_pem(days_until_expiry=365)
        _, mock_sns = self._run_handler(cert_pem)
        mock_sns.publish.assert_not_called()

    def test_ssm_called_with_correct_param_name(self):
        """SSM get_parameter should be called with the configured param name."""
        cert_pem = generate_cert_pem(days_until_expiry=100)
        mock_ssm, _ = self._run_handler(cert_pem)
        mock_ssm.get_parameter.assert_called_once_with(
            Name=MOCK_PARAM_NAME, WithDecryption=True
        )

    def test_cert_already_expired_sends_sns(self):
        """Already-expired certificate (negative days) → SNS publish should be called."""
        cert_pem = generate_cert_pem(days_until_expiry=0)
        _, mock_sns = self._run_handler(cert_pem)
        mock_sns.publish.assert_called_once()


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
