package main

import (
	"fmt"
	"math/rand"
	"net/http"
	"time"
)

func handleRequest(w http.ResponseWriter, r *http.Request) {
	basePath := r.URL.Path

	// Getting HTTP headers
	headers := ""
	for name, values := range r.Header {
		for _, value := range values {
			headers += fmt.Sprintf("%s: %s\n", name, value)
		}
	}

	// Generating a random value for the cookie
	rand.Seed(time.Now().UnixNano())
	randomValue := fmt.Sprintf("%d", rand.Intn(10000))

	// Setting the cookie with the random value
	cookie := &http.Cookie{
		Name:  "random_cookie",
		Value: randomValue,
	}
	http.SetCookie(w, cookie)

	// HTML page with base path, HTTP headers, and cookie information
	html := fmt.Sprintf(`
		<!DOCTYPE html>
		<html>
		<head>
			<title>Base Path and HTTP Headers</title>
		</head>
		<body>
			<h1>Base Path: %s</h1>
			<h2>HTTP Headers:</h2>
			<pre>%s</pre>
			<h2>Cookie Set:</h2>
			<p>Name: %s</p>
			<p>Value: %s</p>
		</body>
		</html>`, basePath, headers, cookie.Name, cookie.Value)

	// Write HTML response
	w.Header().Set("Content-Type", "text/html")
	fmt.Fprint(w, html)
}

func handleSAMLACM(w http.ResponseWriter, r *http.Request) {

	fmt.Println("call handleSAMLACM ")

	// Retrieve the cookie from the request
	/*
		cookie, err := r.Cookie("random_cookie")
		if err != nil {
			fmt.Println("Cookie not found.")
			http.Error(w, "Cookie not found", http.StatusNotFound)
			return
		}
	*/

	cookies := r.Cookies()

	if len(cookies) == 0 {
		// fmt.Fprintln(w, "No cookies found in the request.")
		http.Error(w, "Cookie not found", http.StatusNotFound)
		return
	}

	// Display all cookies
	fmt.Println("Cookies found in the request:")
	for _, cookie := range cookies {
		fmt.Printf("Name: %s, Value: %s\n", cookie.Name, cookie.Value)
	}

	html := fmt.Sprintf(`
		<!DOCTYPE html>
		<html>
		<head>
			<title>Cookie Value</title>
		</head>
		<body>
			<h2>Cookie found %d</h2>
		</body>
		</html>`, len(cookies))

	// Display the cookie value
	w.Header().Set("Content-Type", "text/html")
	fmt.Fprint(w, html)
}

func main() {
	http.HandleFunc("/", handleRequest)
	http.HandleFunc("/home", handleRequest)
	http.HandleFunc("/saml/acs", handleSAMLACM)

	fmt.Println("Server is listening on port 8080...")
	http.ListenAndServe(":8080", nil)
}
