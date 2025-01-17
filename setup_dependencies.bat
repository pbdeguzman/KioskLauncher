echo off

IF EXIST ".\submodules" (
	echo Recreating links...
	rmdir /s /q ".\submodules"
)

mkdir ".\submodules"
echo.

set "cl=%1"
IF NOT [%1] == [] (
    echo Setup symbolic link for submodules cl
    mklink /j ".\submodules\cl\" "..\%cl%"

    echo Copy out folder cl to maven local %userprofile%\.m2\repository\com\global\cl
    xcopy /S /I /Q /Y /F  "..\%cl%\out\cl" "%userprofile%\.m2\repository\com\global\cl\"
    echo.

    echo Modifying app/build.gradle
    call jrepl "VENDOR_NAME" "%cl:~2%" /f app/build.gradle /o -
    call jrepl "VENDOR_SUFFIX" "%cl:~2,1%" /f app/build.gradle /o -
    echo.

    IF "%cl%" == "CLMagicCube" (
        IF EXIST "%userprofile%\.m2\repository\com\magiccube" (
            echo Recreating m2 repository for magiccube sdk...
            rmdir /s /q "%userprofile%\.m2\repository\com\magiccube"
        )

        IF NOT EXIST "%userprofile%\.m2\repository\com\magiccube" (
            echo Will create a folder link for "%userprofile%\.m2\repository\com\magiccube
            mkdir "%userprofile%\.m2\repository\com\magiccube"
        )

        IF EXIST "%userprofile%\.m2\repository\com\magiccube"  (
            echo Setup symbolic link for out folder magic cube to maven local
            echo.
            echo Copy out folder cl magic cube sdk to maven local %userprofile%\.m2\repository\com\global\magiccube
            xcopy /S /I /Q /Y /F  "..\%cl%\out\mcsdk" "%userprofile%\.m2\repository\com\magiccube\mcsdk\"
            echo.
        )
         call jrepl "MINSDK" "28" /f app/build.gradle /o -
         GOTO END
    )

    IF "%cl%" == "CLSunmi" (
        IF EXIST "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release" (
            echo Recreating m2 repository for sunmi sdk...
            rmdir /s /q "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release"
        )

        IF NOT EXIST "%userprofile%\.m2\repository\\com\sunmi\paylib\PayLib-release" (
            echo Will create a folder link for "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release.
            mkdir "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release"
        )

        IF EXIST "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release"  (
            echo Setup symbolic link for out folder sunmi to maven local
            echo.
            echo Copy out folder cl sunmi maven local %userprofile%\.m2\repository\com\global\sunmi
            xcopy /S /I /Q /Y /F  "..\%cl%\out\PayLib-release" "%userprofile%\.m2\repository\com\sunmi\paylib\PayLib-release"
            echo.
        )

        IF EXIST "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static" (
            echo Recreating m2 repository for sunmi sdk...
            rmdir /s /q "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static"
        )

        IF NOT EXIST "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static" (
            echo Will create a folder link for "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static.
            mkdir "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static"
        )

        IF EXIST "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static"  (
            echo Setup symbolic link for out folder sunmi to maven local
            echo.
            echo Copy out folder cl sunmi maven local %userprofile%\.m2\repository\com\global\sunmi
            xcopy /S /I /Q /Y /F  "..\%cl%\out\mmkv-static" "%userprofile%\.m2\repository\com\tencent\mmkv\mmkv-static"
            echo.
        )
    )

    IF "%cl%" == "CLVerifone" (
        IF EXIST "%userprofile%\.m2\repository\com\verifone" (
            echo Recreating m2 repository for usb comm for verifone sdk...
            rmdir /s /q "%userprofile%\.m2\repository\com\verifone"
        )

        IF NOT EXIST "%userprofile%\.m2\repository\com\verifone" (
            echo Will create a folder link for "%userprofile%\.m2\repository\com\verifone.
            mkdir "%userprofile%\.m2\repository\com\verifone"
        )

        IF EXIST "%userprofile%\.m2\repository\com\verifone"  (
            echo Setup symbolic link for out folder usb comm verifone to maven local
            echo.
            echo Copy out folder cl verifone usbcomm sdk to maven local %userprofile%\.m2\repository\com\verifone\usbconnman
            xcopy /S /I /Q /Y /F  "..\%cl%\out\UsbConnManLib" "%userprofile%\.m2\repository\com\verifone\usbconnman\"
            echo.
        )

        echo For CLVerifone gradle minSDK
        call jrepl "MINSDK" "27" /f app/build.gradle /o -
        GOTO END
    )

    echo Other CLs
    call jrepl "MINSDK" "22" /f app/build.gradle /o -

) ELSE (
    echo No CLs
    call jrepl "MINSDK" "22" /f app/build.gradle /o -

)

:END
EXIT /B