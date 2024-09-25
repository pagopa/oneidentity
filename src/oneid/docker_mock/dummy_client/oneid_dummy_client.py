#from attr import fields
from urllib3 import PoolManager
from http import HTTPStatus
from urllib.parse import urlencode
import json
import base64
import uuid
import os
from flask import Flask, redirect, url_for, Response, request

app = Flask(__name__)

client_id = os.getenv('client_id', '')
client_secret = os.getenv('client_secret', '')
authorizer_url = 'http://localhost:8080/login'
redirect_url = 'http://localhost:8084/client/cb'
token_url = "http://oneid-ecs-core:8080/oidc/token"

@app.route('/')
def index():
    return redirect(url_for('login'))

@app.get("/client/login")
def login():
    nonce = str(uuid.uuid4().hex)
    return Response(
        status= HTTPStatus.OK.value,
        content_type="text/html",
        response="<html lang=\"en-US\">\n" +
                "<head>\n" +
                "    <title>PagoPA example product</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>PagoPA example product</h1>\n" +
                "        <div id=\"content\">\n" +
                " <form action=\""+ authorizer_url +"\" method=\"GET\"> " +
                " <input type=\"hidden\" name=\"response_type\" value=\"CODE\"> " +
                " <input type=\"hidden\" name=\"scope\" value=\"openid\"> " +
                " <input type=\"hidden\" name=\"client_id\" value=\""+ client_id +"\"> " +
                " <input type=\"hidden\" name=\"state\" value=\"state1\"> " +
                " <input type=\"hidden\" name=\"nonce\" value=\"" +nonce +"\"> " +
                " <input type=\"hidden\" name=\"redirect_uri\" value=\""+ redirect_url +"\"> " +
                " <button type=\"submit\" class='btn btn-primary'>Entra con SPID/CIE</button>" +
                " </form>" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"
    )


@app.get("/client/cb")
def callback():

    code = request.args.get('code')
    # TODO state should be verified by the client (is it a correct request?)
    state = request.args.get('state')

    url = token_url #+ "?" + get_form_data_as_string(form_data)
    base64string = base64.b64encode(bytes('{}:{}'.format(client_id, client_secret), 'ascii'))
    

    encoded_body = {
        "grant_type": "AUTHORIZATION_CODE",
        "code": code,
        "redirect_uri": redirect_url,
    }

    http = PoolManager() #TODO do we still need this?
    r = http.request('POST', url,
                 headers={
                     "Accept": "application/json",
                     #"Content-type": "application/x-www-form-urlencoded",
                     "Authorization": "Basic {}".format(base64string.decode('utf-8'))
                    },
                 fields=encoded_body,
                 encode_multipart = False
                 )

    json_response = json.loads(r.data)
    print(json_response)
    jwt = json_response["id_token"]
    payload = json.loads(base64.b64decode(jwt.split('.')[1] + '==').decode())
    # signature not verified, we're a dummy client
    header = json.loads(base64.b64decode(jwt.split('.')[0] + '==').decode())

    # return_value = (
    #     f"IDToken: {json_response['id_token']}\n"
    #     f"Access Token: {json_response['access_token']}\n"
    #     f"FiscalNumber: {payload['fiscalNumber']}\n"
    #     f"Headers: {header}\n"
    # )

    return Response(
        status= HTTPStatus.OK.value,
        content_type="text/html",
        response= "<!DOCTYPE html> " +
        "<html lang=\"en\">  \n" + 
            "<head>  \n" +
            "<meta charset=\"UTF-8\">  \n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">  \n" +
            "<title>Data</title>  \n" +
            "<style>  \n" +
            "    body {  \n" +
            "        font-family: Arial, sans-serif;  \n" +
            "        margin: 0;  \n" +
            "        padding: 20px;  \n" +
            "        background-color: #f4f4f4;  \n" +
            "    }  \n" +
            "    .container {  \n" +
            "        max-width: 600px;  \n" +
            "        margin: 0 auto;  \n" +
            "        background-color: #fff;  \n" +
            "        border-radius: 8px;  \n" +
            "        padding: 20px;  \n" +
            "        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);  \n" +
            "    }  \n" +
            "    h1 {  \n" +
            "        text-align: center;  \n" +
            "        color: #333;  \n" +
            "    }  \n" +
            "    .token {  \n" +
            "        background-color: #f9f9f9;  \n" +
            "        padding: 10px;  \n" +
            "        margin-bottom: 20px;  \n" +
            "        border-radius: 4px;  \n" +
            "        word-wrap: break-word;  \n" +
            "    }  \n" +
            "    label {  \n" +
            "        font-weight: bold;  \n" +
            "    }  \n" +
            "    .code {  \n" +
            "        font-family: 'Courier New', Courier, monospace;  \n" +
            "    }  \n" +
            "</style>  \n" +
            "</head>  \n" +
            "<body>  \n" +
            "<div class=\"container\">  \n" +
            "    <h1>Token e Codici</h1>  \n" +
            "    <div class=\"token\">  \n" +
            "        <label>Access Token:</label><br>  \n" +
            "        <code class=\"code\">"+ json_response['access_token'] +"</code>  \n" +
            "    </div>  \n" +
            "    <div class=\"token\">  \n" +
            "        <label>ID Token:</label><br>  \n" +
            "        <code class=\"code\">"+ json_response['id_token']+"</code>  \n" +
            "    </div>  \n" +
            "    <div class=\"token\">  \n" +
            "        <label>Codice Fiscale:</label><br>  \n" +
            "        <code class=\"code\">"+ payload['fiscalNumber'] +"</code>  \n" +
            "    </div>  \n" +
            "    <div class=\"token\">  \n" +
            "        <label>Nonce:</label><br>  \n" +
            "        <code class=\"code\">"+ payload['nonce'] +"</code>  \n" +
            "    </div>  \n" +
            "    <div class=\"token\">  \n" +
            "        <label>Authorization Code:</label><br>  \n" +
            "        <code class=\"code\">"+ code +"</code>  \n" +
            "    </div>  \n" +
            "</div>  \n" +
            "</body>  \n" +
        "</html>"
    )

if __name__ == '__main__':
	app.run(host='0.0.0.0', port=8084)
