# from attr import fields
from urllib3 import PoolManager
from urllib.parse import urlencode
import json
import base64
import uuid
import os
from flask import Flask, redirect, url_for, request, render_template

app = Flask(__name__)

client_id = os.getenv("client_id", "")
client_secret = os.getenv("client_secret", "")
authorizer_url = "http://localhost:8080/login"
redirect_url = "http://localhost:8084/client/cb"
token_url = "http://oneid-ecs-core:8080/oidc/token"


@app.route("/")
def index():
    return redirect(url_for("login"))


@app.get("/client/login")
def login():
    nonce = str(uuid.uuid4().hex)
    return render_template(
        "./login.html",
        authorizer_url=authorizer_url,
        client_id=client_id,
        nonce=nonce,
        redirect_url=redirect_url,
    )


@app.get("/client/cb")
def callback():

    code = request.args.get("code")
    # TODO state should be verified by the client (is it a correct request?)
    state = request.args.get("state")

    url = token_url  # + "?" + get_form_data_as_string(form_data)
    base64string = base64.b64encode(
        bytes("{}:{}".format(client_id, client_secret), "ascii")
    )

    encoded_body = {
        "grant_type": "AUTHORIZATION_CODE",
        "code": code,
        "redirect_uri": redirect_url,
    }

    http = PoolManager()  # TODO do we still need this?
    r = http.request(
        "POST",
        url,
        headers={
            "Accept": "application/json",
            "Authorization": "Basic {}".format(base64string.decode("utf-8")),
        },
        fields=encoded_body,
        encode_multipart=False,
    )

    json_response = json.loads(r.data)
    print(json_response)
    jwt = json_response["id_token"]
    payload = json.loads(base64.b64decode(jwt.split(".")[1] + "==").decode())
    # signature not verified, we're a dummy client

    return render_template(
        "data.html",
        access_token=json_response["access_token"],
        id_token=json_response["id_token"],
        fiscalNumber=payload["fiscalNumber"],
        nonce=payload["nonce"],
        code=code,
    )


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8084)
