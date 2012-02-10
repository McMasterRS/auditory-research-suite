/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av;

/**
 * @author bguseman
 *
 */
public interface Scheduled {
	public void markTime(ScheduleEvent e);
	public void alarm(ScheduleEvent e);
}
