package com.dezier.utils;

interface IShellUtilService {
    void destroy() = 16777114;
    void exit() = 1;
    int grantOverlayPermission(String packageName, int uid) = 2;
    int grantAccessibilityPermission(String componentName) = 3;
}