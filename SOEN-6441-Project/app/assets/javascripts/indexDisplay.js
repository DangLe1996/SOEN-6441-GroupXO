ws = new WebSocket("ws://" + location.host + "/indexWs")



function addItem(){
        const searchTerm = document.getElementsByName("searchString")[0].value;
    const elementExists = document.getElementById(searchTerm);
    if(elementExists == null){
        ws.send(JSON.stringify({queryTerm: searchTerm}));
    }
    else{

        const div = document.getElementById("myDiv");
        div.removeChild(elementExists);
        div.prepend(elementExists);


    }
    document.getElementsByName("searchString")[0].value = "";

}

ws.onopen = function() {
    console.log("I am open ws");
    $('#myDiv table').each(function(){
        ws.send(JSON.stringify({queryTerm: this.id}));
    });
}


ws.onmessage = function(event){

    const message = JSON.parse(event.data);
    switch (message.type){
        case "AddNewQuery":
            populateNewTweet(message['htmlCode'],message.queryTerm )

    }


    console.log(event.data);
}
function populateNewTweet( msg, searchWord){
    const div = document.getElementById("myDiv");
    const elementExists = document.getElementById(searchWord);
    if(elementExists != null){
        elementExists.innerHTML = msg;
    }
    else {
        div.innerHTML = msg + div.innerHTML;
    }


}