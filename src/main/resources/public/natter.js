const apiUrl = "https://localhost:4567"

window.addEventListener("load", (e) => {
  document.getElementById("createSpace").addEventListener("submit", processFormSubmit)
})

function processFormSubmit(e) {
  e.preventDefault()

  let spaceName = document.getElementById("spaceName").value
  let owner = document.getElementById("owner").value

  createSpace(spaceName, owner)

  return false
}

function createSpace(name, owner) {
  let data = { name, owner }

  fetch(apiUrl + "/spaces", {
    method: "POST",
    credentials: "include",
    body: JSON.stringify(data),
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (response.ok) {
        return response.json()
      } else {
        throw new Error(response.statusCode)
      }
    })
    .then((json) => console.log("Created space: ", json.name, json.uri))
    .catch((error) => {
      console.error("Error: ", error)
    })
}
