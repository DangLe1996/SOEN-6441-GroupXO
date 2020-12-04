

ws = new WebSocket("ws://" + location.host + "/keywordWs")

const queryString = window.location.search;
const urlParams = new URLSearchParams(queryString);
const product = urlParams.get('s')

ws.onopen = () => ws.send(product);

ws.onmessage = function(event){
 	 $("#keywordstats").empty()
     $("#keywordstats").html("<table align=center>"  + event.data + "</table>");

}
