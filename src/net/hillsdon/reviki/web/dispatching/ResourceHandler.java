package net.hillsdon.reviki.web.dispatching;

import net.hillsdon.reviki.web.common.RequestHandler;

/**
 * Manages the /resources special section of any wiki.
 * 
 * The motivation is not requiring the root of the web-app to be public
 * (the base URL is configurable on a per wiki basis).
 * 
 * @author mth
 */
public interface ResourceHandler extends RequestHandler {
}
