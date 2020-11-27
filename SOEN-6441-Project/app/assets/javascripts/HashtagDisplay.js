

ws = new WebSocket("ws://" + location.host + "/HashTagWs")

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const product = urlParams.get('hashTag')


ws.onopen = () => ws.send(product);

ws.onmessage = function(event){


    console.log(event.data);

}
