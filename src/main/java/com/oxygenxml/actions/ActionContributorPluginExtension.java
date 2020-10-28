package com.oxygenxml.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.actions.AuthorActionsProvider;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.listeners.WSEditorListener;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * Plugin extension - workspace access extension.
 */
public class ActionContributorPluginExtension implements WorkspaceAccessPluginExtension {
  private AuthorPopupMenuCustomizer popUpCustomizer;
  
  String anchorActionID = null;
  String targetActionID = null;
  String actionDefaultkeystroke = null;
  
  private boolean keystrokeSet = false;

  private StandalonePluginWorkspace pluginWorkspaceAccess;
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    loadProps();
    

    popUpCustomizer = (popUp, authorAccess) -> {
      if (targetActionID != null && targetActionID.length() > 0) {
        AuthorActionsProvider actionsProvider = authorAccess.getEditorAccess().getActionsProvider();
        Map<String, Object> authorCommonActions = actionsProvider.getAuthorCommonActions();
        Action targetAction = (Action) authorCommonActions.get(targetActionID);

        if (targetAction != null) {
          Action wrapperAction = createWrapper(targetAction);
          JPopupMenu menui = (JPopupMenu) popUp;
          int index = getAnchorIndex(actionsProvider, menui);
          menui.insert(wrapperAction, index);
        }
      }
    };
    
    if (targetActionID != null && targetActionID.length() > 0) {
      pluginWorkspaceAccess.addEditorChangeListener(new WSEditorChangeListener() {
        @Override
        public void editorOpened(java.net.URL editorLocation) {
          WSEditor editorAccess = pluginWorkspaceAccess.getEditorAccess(editorLocation, PluginWorkspace.MAIN_EDITING_AREA);

          installOnEditor(editorAccess);
        }
      } , PluginWorkspace.MAIN_EDITING_AREA);
      
      URL[] allEditorLocations = pluginWorkspaceAccess.getAllEditorLocations(PluginWorkspace.MAIN_EDITING_AREA);
      for (int i = 0; i < allEditorLocations.length; i++) {
        installOnEditor(pluginWorkspaceAccess.getEditorAccess(allEditorLocations[i], PluginWorkspace.MAIN_EDITING_AREA));
      }
    }
    
  }
  
  private void installOnEditor(WSEditor editorAccess) {
    install(editorAccess.getCurrentPage());

    editorAccess.addEditorListener(new WSEditorListener() {
      @Override
      public void editorPageChanged() {
        install(editorAccess.getCurrentPage());
      };
    });
  }

  private void loadProps() {
    targetActionID = System.getProperty("actions.actionID");
    anchorActionID = System.getProperty("actions.anchorActionID");
    actionDefaultkeystroke = System.getProperty("actions.defaultKeyStroke");
  }

  private int getAnchorIndex(AuthorActionsProvider actionsProvider, JPopupMenu menui) {
    int index = 0;
    if (anchorActionID != null && anchorActionID.length() > 0) {
      for (int i = 0; i < menui.getComponentCount(); i++) {
        Component component = menui.getComponent(i);
        if (component instanceof JMenuItem) {
          Action action = ((JMenuItem) component).getAction();
          if (action != null ) {
            String actionID = actionsProvider.getActionID(action);
            if (anchorActionID.equals(actionID)) {
              index = i + 1;
              break;
            }
          }
        }
      }
    }
    return index;
  }

  private AbstractAction createWrapper(Action targetAction) {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        targetAction.actionPerformed(e);
      }

      @Override
      public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key) && actionDefaultkeystroke != null) {
          return KeyStroke.getKeyStroke(com.oxygenxml.actions.KeystrokeUtil.convertToPlatformDependentKeystroke(actionDefaultkeystroke, " "));
        }
        return targetAction.getValue(key);
      }
    };
  }
  
  private void install(WSEditorPage currentPage) {
    if (currentPage instanceof WSAuthorEditorPage) {
      WSAuthorEditorPage wsAuthorEditorPage = (WSAuthorEditorPage) currentPage;

      wsAuthorEditorPage.removePopUpMenuCustomizer(popUpCustomizer);
      wsAuthorEditorPage.addPopUpMenuCustomizer(popUpCustomizer);
      
      setKeystroke(wsAuthorEditorPage);
    }
  }

  private void setKeystroke(WSAuthorEditorPage wsAuthorEditorPage) {
    if (!keystrokeSet
        && targetActionID != null && targetActionID.length() > 0 
        && actionDefaultkeystroke != null && actionDefaultkeystroke.length() > 0) {
      AuthorActionsProvider actionsProvider = wsAuthorEditorPage.getActionsProvider();
      Map<String, Object> authorCommonActions = actionsProvider.getAuthorCommonActions();
      Action targetAction = (Action) authorCommonActions.get(targetActionID);
      if (targetAction != null) {
        AbstractAction action = createGlobalAction();
        action.putValue(Action.NAME, targetAction.getValue(Action.NAME));
        action.putValue(Action.SHORT_DESCRIPTION, targetAction.getValue(Action.SHORT_DESCRIPTION));
        pluginWorkspaceAccess.getActionsProvider().registerAction(targetActionID +"_Custom", action, actionDefaultkeystroke);
      }

      keystrokeSet = true;
    }
  }

  private AbstractAction createGlobalAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        WSEditor currentEditorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        
        WSEditorPage currentPage = currentEditorAccess.getCurrentPage();
        if (currentPage instanceof WSAuthorEditorPage) {
          Action cAction = (Action) ((WSAuthorEditorPage) currentPage).getActionsProvider().getAuthorCommonActions().get(targetActionID);
          if (cAction != null) {
            cAction.actionPerformed(null);
          }
        }
      }
    };
  }

  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
	  //You can reject the application closing here
    return true;
  }
}