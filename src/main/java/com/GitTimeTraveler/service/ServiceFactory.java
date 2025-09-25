package com.GitTimeTraveler.service;

import com.GitTimeTraveler.service.impl.*;

/**
 * Factory class for creating service instances
 */
public class ServiceFactory {
    
    private static final GitService gitService = new GitServiceImpl();
    private static final PatternService patternService = new PatternServiceImpl();
    private static final FileService fileService = new FileServiceImpl();
    private static final UIService uiService = new UIServiceImpl();
    
    /**
     * Get GitService instance
     */
    public static GitService getGitService() {
        return gitService;
    }
    
    /**
     * Get PatternService instance
     */
    public static PatternService getPatternService() {
        return patternService;
    }
    
    /**
     * Get FileService instance
     */
    public static FileService getFileService() {
        return fileService;
    }
    
    /**
     * Get UIService instance
     */
    public static UIService getUIService() {
        return uiService;
    }
}
