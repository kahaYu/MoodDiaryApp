# MoodDiaryApp

Do you want to **track your mood** for a distance? <br>
Meet **Mood Diary!** :book:

Actually it doesn't do much like it's older brothers.
But it perfectly illustrates **skills and technologies I posses.**

## In details:
1. MVVM + Room + ViewModel;
2. ViewPager2 + FragmentStateAdapter;
3. DataBinding;
4. Coroutines;
5. Fragments;
6. LiveData;
7. Channels;
8. Dependency injection with Hilt;
9. SharedPreferences.

:tada: **Great!** You've reached the most interesting part - pictures! <br>
And of course, complications I faced on my way. :dizzy_face:

## What it does?

**:one: When you launch the App, dao queries SQL-table and activity prepopulates viewpager with last data, stored in database.**

<img src="https://user-images.githubusercontent.com/79222385/149162936-bc58d29b-7670-4930-b994-c55a9b3e69a0.jpg" width="360" height="640">

**:two: Tap fab and you'll face dialog for adding new note.** <br>
To round corners I removed standard background and put own XML-drawable.<br>

<img src="https://user-images.githubusercontent.com/79222385/149165241-b970163c-1eeb-4f13-97e6-536c26877afe.jpg" width="360" height="640">

**:three: Change rate in drop-down list and mini-image on preview will also be chaged.** <br>

To achieve this I:
1. Observe changes in drop-down list;
2. Send new value to viewmodel's function that calculates proper image, based on the value;
3. Fire livedata with new image;
4. Observe livedata property of viewmodel in XML with help of **DataBinding**.<br>

<img src="https://user-images.githubusercontent.com/79222385/149166814-26032f3b-9219-47d3-a6c0-117208f4a3a9.jpg" width="360" height="640">

**:four: Apply changes and new note will appear on the screen.** <br>
Under the hood I clear all notes from the screen and inflate totally new list of notes. 
I need doing it, because sorting and filtration could be ON. So I always have to check it and process new list.
It can be optimized in future, but it's out of my scope currently.

**:five: Tap the existing note and you can update or delete it.** <br>
Updating dialog is the same class as dialog for adding note. 
It's bad practice, cause violates first SOLID principle about single responsibility.
But it illustrates my skill of making dynamic layout. Depending on whether it's adding or updating dialog Delete-button presents or not. Dialog-name is also changed.

***Update***

<img src="https://user-images.githubusercontent.com/79222385/149327192-7fdd9ac4-bbb0-4925-97ac-53f177a06a02.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149327695-355093f0-f3b8-44da-97bb-b1cd39f2c3ca.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149327837-8595ebf8-bdad-4f95-ba6d-3f4180ae0d96.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149328309-07ff80bb-7ae3-4fae-a4c6-309c7ae8989d.jpg" width="230" height="409">

***Delete*** | Of course you might deleted it accidentally. No problem - undo deletion!

<img src="https://user-images.githubusercontent.com/79222385/149329077-754efe17-c9dd-4afd-b535-6740206036d1.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149329093-3f018aa4-f0de-4514-aeae-e76d6e525c89.jpg" width="230" height="409">

**:six: Filtration.** <br>
DataBinding rules the ball. I fire live data right from XML. <br>
Apply and Reset-buttons are different views. Appears and disappears depending on state of filtration. <br>
Filtration and sorting also cleans all views and then inflates again the new list. It's made with help of MediatorLiveData. It combines different sourses of data.

<img src="https://user-images.githubusercontent.com/79222385/149334023-9da1b773-5b75-421d-9232-9e6ef9ca201c.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149334044-3804e4bb-0aa2-452c-a495-ffec8b08df7a.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149334070-c3893639-8194-4187-b38a-d98dd93957ec.jpg" width="230" height="409">

**:seven: Sorting.** <br>
Works even in combination with filtration.
All using MediatorLiveData. <br>
When sort or filtration is ON, orange indicator close to icon appears in Navigation bar. 

***Sort in combination with filter***

<img src="https://user-images.githubusercontent.com/79222385/149335087-0e54f1db-8cda-47b8-94b3-6cd2385b1b31.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149335094-91c083d3-17ff-43db-94fe-71735e065f46.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149335111-c67654b4-0eec-4bf7-8878-b6a5d77006ba.jpg" width="230" height="409">

***Reset filter. Sort only***

<img src="https://user-images.githubusercontent.com/79222385/149335123-618aad68-f5ff-4a27-8677-39e5e7ab285c.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149335127-71c0d95e-eb62-45c4-8cf7-a488cc342804.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149335132-528b9962-994b-493a-86ed-4c80cc4cd024.jpg" width="230" height="409">

**:eight: All pages above 3 are deleted.** <br>
If you add 19th note, first 6 are deleted. Just not to overload dots indicator.
It works also if sorting and filtration is ON.

<img src="https://user-images.githubusercontent.com/79222385/149338838-bb1a795a-bed2-4ebe-99cc-a468303e0767.jpg" width="360" height="640">

**:nine: No problem to delete all notes.** <br>

<img src="https://user-images.githubusercontent.com/79222385/149339472-a25108b9-5452-4e88-9ae2-1f215c83dd0d.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149339478-dc1c26ce-5153-40b3-b3e1-e4aeab0fb222.jpg" width="230" height="409">

**:one::zero: You can't estimate one day twice until point it explicitly.** <br>
If you tap Add-fab secondly in one day, you face confirmation dialog.
App remembers your choose with help of SharedPreferences.

<img src="https://user-images.githubusercontent.com/79222385/149340218-13127e2a-415e-4b71-b988-e3a06f9d3ca5.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149340242-5c85b2ae-25b2-4a0b-ba71-078c5fb738e1.jpg" width="230" height="409"> <img src="https://user-images.githubusercontent.com/79222385/149340256-00007c2d-f147-45ee-b7d7-2e78f9549f7d.jpg" width="230" height="409">

## Most challenging issues I had:

1. ViewPager is responsible for managing pages. **But who is responsible for managing views on these pages?** <br>
*- Right! Nobody.* :satisfied: <br>
&nbsp;&nbsp;&nbsp;&nbsp;Issue was to inflate, delete, update views according to the same operations in database. <br>
:muscle:RecyclerView does this all for you. It's enought to feed the adapter with updated data and it will populate screen accordingly.<br>
But in ViewPager I have to rule it by myself. It's kinda I wrote my own adapter. :white_check_mark: 

2. To force filter and sort **work independently**. <br>
&nbsp;&nbsp;&nbsp;&nbsp;First what I tried was to get all notes in MainActivity from single LiveData. And after it to make filtration and sorting in MainActivity. Bad practice, :grimacing: but I wanted to realise it and feel all disadvantages by myself. <br>
After hours of trying I almost set it properly. But still, there were no full independence. And I can't reach it. <br>
&nbsp;&nbsp;&nbsp;&nbsp;I started **to dig deeper** and learned about **dynamic SQL-quieries**. Dao's function receives paramentres of filtration and sorting and return prepared list of notes to transfer to MainActivity. To my big regret, my query didn't want to work.:weary: After 2 days of trying I found somewhere, that it's overkill to delegate processing of the list to SQL. They say it's beter to do by yourself in the code. Were they right? :pray: <br>
&nbsp;&nbsp;&nbsp;&nbsp;I made a decision to do so. But I couldn't figure out how to update list of notes **every time some parameter is changed**. I saw lessons only using Flow's operator *.combine{}*. But I don't wanna use Flows in this App. Purpose was to learn the LiveData. Digging in, universe sent me an angel, even presented me a life - **MediatorLIFEData** :stuck_out_tongue_winking_eye: <br>
With help of it I easy combined 6 sources of data and update list every time any data is changed. MainActivity receives **only notes for displaying**. It corresponds with the purpose of MVVM - to devide layers, to leave as little operations in UI-layer as possible. 

### Thank you for attention! :raised_hands:
:sparkles: Wish you having a built in filter to see only good things in life, which are from 9 and bigger according to the App's scale!

