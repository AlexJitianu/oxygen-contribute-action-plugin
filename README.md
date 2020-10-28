# oxygen-contribute-action-plugin
A plugin that puts back an action that was removed by another plugin.

## Configuration
Download the release, unzip, and edit plugin.xml . The values of these three properties control which action should be retrieved:

```
  <!-- ID of the action to bring back in the contextual menu. -->
  <property name="actions.actionID" value="Author/Edit_Profiling_Attributes"/>
 <!-- ID of another action present in the contextual menu that should serve as an anchor. -->
  <property name="actions.anchorActionID" value="Edit/Edit_attributes"/>
 <!-- A keystroke for the returned action
  
   * The "M" modifier keys are a platform-independent way of representing keys, 
   * and these are generally preferred. 
   * - M1 is the COMMAND key on MacOS X, and the CTRL key on most other platforms. 
   * - M2 is the SHIFT key. 
   * - M3 is the Option key on MacOS X, and the ALT key on most other platforms. 
   * - M4 is the CTRL key on MacOS X, and is undefined on other platforms. 
   * 
  
  . -->
 <property name="actions.defaultKeyStroke" value="M1 M3 X"/>
```

## Deploy
Unzip it inside Oxygen's `plugins` directory.
