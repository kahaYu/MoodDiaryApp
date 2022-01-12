# MoodDiaryApp

Do you want to **track your mood** for a distance? <br>
Meet **Mood Diary!** :book:

Actually it doesn't do much like it's older brothers.
But it perfectly illustrates **skills and technologies I posses.**

## In details:
1. MVVM + Room + ViewModel;
2. ViewPager2 + FragmentStateAdapter;
3. DataBinding;
4. Fragments;
5. LiveData;
6. Channels;
7. Dependency injection with Hilt;
8. SharedPreferences.

**Great!** You've reached the most interesting part - pictures! <br>
And of course, complications I faced on my way. :dizzy_face:

## What it does?

**1. When you launch the App, dao queries SQL-table and activity prepopulates viewpager with last data, stored in database.**

<img src="https://user-images.githubusercontent.com/79222385/149162936-bc58d29b-7670-4930-b994-c55a9b3e69a0.jpg" width="360" height="640">

**2. Tap fab and you'll face dialog for adding new note.** <br>
To round corners I removed standard background and put own XML-drawable.<br>

<img src="https://user-images.githubusercontent.com/79222385/149165241-b970163c-1eeb-4f13-97e6-536c26877afe.jpg" width="360" height="640">

**3. Change rate in drop-down list and mini-image on preview will also be chaged.** <br>

To achieve this I:
1. Observe changes in drop-down list;
2. Send new value to viewmodel's function that calculates proper image, based on the value;
3. Fire livedata with new image;
4. Observe livedata property of viewmodel in XML with help of **DataBinding**.<br>

<img src="https://user-images.githubusercontent.com/79222385/149166814-26032f3b-9219-47d3-a6c0-117208f4a3a9.jpg" width="360" height="640">

**4. Apply changes and new note will appear on the screen.** <br>
Under the hood I clear all notes from the screen and inflate totally new list of notes. 
I need doing it, because sorting and filtration could be ON. So I always have to check it and process new list.
It can be optimized in future, but it's out of my scope currently.

