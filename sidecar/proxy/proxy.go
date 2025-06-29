package proxy

import (
	"fmt"
	"net/http"
	"sidecar/config"
)

func DynamicHandler(w http.ResponseWriter, r *http.Request) {
	path := r.URL.Path
	baseURL := fmt.Sprintf("http://%s:%s/problems/%s/%s", config.ProxyIP, config.ProxyPort, config.ProblemID, config.UUID)

	redirectURL := fmt.Sprintf("%s%s", baseURL, path)
	http.Redirect(w, r, redirectURL, http.StatusFound)
}
