The functions and things that I did are as follows:

1. created a master JSON file that is empty on startup on a new device.
    There is a check to see if the file is empty, if it is, a new file is
    created with the right JSON format.
2. Utilized the internal storage to store the files
3. the JSON is read into the list using the grabJSONData() function
4. whenever the add button is pressed there is a dialog that pops
    up and asks the user what the new lists name is.
5. the addList() function adds the new item in the dialog and then calls
    the insertIntoMaster() function
6. the insertIntoMaster() function takes the old master JSON file and inserts 
    the new item in. the function then creates a new file in the internal
    storage. the naming convertion is the name of the list lower case and
    .json attached to the end.
7. I made a new Location class inside TodoList so that I can know if there
    is is a location for that list or not.

Important things to do:
1. Change the list XML layout so that it looks like the one I designed.
2. Read and write to the list JSON files so that the data can be saved
    across sessions.
3. Find out how to make an undo button so that the lists don't dissapear
    when accidentally swiped. there should be a link in the README.md file
    also included in the .zip

There is no "dummy data", so you need to input the data when you first
    run the program. The master list savesdata, but not the individual
    lists.

Feel free to use any of the code that I wrote to help with your coding.
    Just ask if a function or process doesn't make sense to you.

I used the File class to do all of the input and output.
You can't write to the assets folder during run time, it cannot be expanded 
    oncethe app is built. use either internal storage, external storage
    or shared preferences.

This is the format for the master list:
{
    "master":[
        {
            "name":"Broulim's"
        },
        {
            "name":"Walmart"
        }
    ]
}

The JSONArray is named master and each item only has one value, name. 
    This name value is the string that will display on the master list and
    be reference in the list's JSON and the title value in that JSON.

This is the format for the individual lists:
broulim's.json
{
    "title":"Broulim's",
    "location":{
        "lat":0,
        "long":0,
        "address":""
    },
    "items":[
        {
            "name":"eggs",
            "history":false
        },
        {
            "name":"bread",
            "history":true
        }
    ]
}

You don't need to worry about setting the location, The empty JSON file
    for the list will have take care of it. The title is already accounted
    for when the file is made. (See lines 139-155 if needed)

The items page will need to look roughly like this:
-------------------------------------
|"title"                            |
|"address"                          |
|"item1"                            |
|"item2"                            |
|"item3"                            |
|"item4"                            |
|"item5"                            |
|"item6"                            |
|"item7"                            |
|empty space                   add  |
-------------------------------------

The title, address and the empty space at the bottom need to be fixed in 
    place. the items need to be in the list. Look at the code that I
    wrote if you need to knwo who to insert the information into the
    list so that the list is sorted. The code is lines 288-297. The 
    method that I used to saved the data so that it's sorted is on lines
    107-131. This code reads the JSON from the file and then places it
    into an array so that the Collections Singleton can sort the data.
    The data is put back into a JSONArray and then into a JSONObject 
    then the JSON string overwrites the original. If there is no list
    data for the new list, then there is a new json file created. Please
    find a way to save the list data into those JSON files so that the
    user can get the information when they load the app next.