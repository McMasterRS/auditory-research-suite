/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.common.datamodel;

import java.io.IOException;

/**
 * Interface for classes supplying trial logging services.
 * @version     $Revision:$
 * @author     <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since     Nov 22, 2006
 * @param <B> Block type
 * @param <T> Trial type
 */
public interface TrialLogger<B extends Block<?,?>, T extends Trial<?>> {

    /**
     * Save the current state of the session object to the database.
     * 
     * @throws IOException on persistence error.
     */
    public abstract void saveSessionConfig() throws IOException;

    /**
     * Submit the given trial to the database. Should be called after a response
     * has been recorded.
     * 
     * @param trial object to record
     */
    public abstract void submit(B block, T trial) throws IOException;

    /**
     * To be called by the experiment when experiment is over to indicate
     * to logger that resources may be released or cleaned up.
     */
    public abstract void shutdown();

}