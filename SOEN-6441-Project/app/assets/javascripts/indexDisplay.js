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
            populateNewTweet(message['htmlCode'],message.queryTerm );
            break;
        case "UpdateQuery":
            updateTable(event.data);
            break;
    }

}

function updateTable(msg){
     deleterow(msg);
     addRow( msg);

}

function addRow( message) {

    var sentiMentStart= message.indexOf("<CUSTOMSENTIMENT>");
    var sentiMentEnd= message.indexOf("</CUSTOMSENTIMENT>");
    var sentimentMsg=message.substr(sentiMentStart+17,sentiMentEnd-sentiMentStart-17);
    var renderMsg=message.replace(sentimentMsg,"");
    const dataToAdd = JSON.parse(renderMsg);

    //const dataToAdd = JSON.parse(message);

    // Get a reference to the table
    let tableRef = document.getElementById(dataToAdd.queryTerm);
    let ths = document.getElementById(dataToAdd.queryTerm).getElementsByTagName('th');
    ths[2].innerText=sentimentMsg;

    // Insert a row at the end of the table
    let newRow = tableRef.insertRow(2);

    newRow.innerHTML = dataToAdd.htmlCode;

}


function deleterow(message) {
    const dataToAdd = JSON.parse(message);

    var table = document.getElementById(dataToAdd.queryTerm);
    var rowCount = table.rows.length;

    table.deleteRow(rowCount -1);
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