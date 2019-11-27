# FragmentHelper

The utility class for managing fragments inside activity.

## Usage

Execute this command under your project root:

    git submodule add https://github.com/KivApple/FragmentHelper.git fragmenthelper

Add these lines to your Android project files:

`/settings.gradle`

    include ':app', ..., ':fragmenthelper'

`/build.gradle`

    buildscript {
        ext.kotlin_version = '1.3.50'
        ext.appCompatVersion = '1.0.2'
        ext.materialVersion = '1.0.0'
        ...
    }

`/app/build.gradle`

    dependencies {
        ...
        implementation project(':fragmenthelper')
        ...
    }
