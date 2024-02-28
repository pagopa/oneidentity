package main

import (
	"fmt"
	"net/http"
)

func handleRequest(w http.ResponseWriter, r *http.Request) {
	path := r.URL.Path

	message := fmt.Sprintf("You requested: %s", path)

	fmt.Fprintf(w, message)
}

func main() {

	http.HandleFunc("/", handleRequest)

	fmt.Println("Server is listening on port 8080...")

	http.ListenAndServe(":8080", nil)
}