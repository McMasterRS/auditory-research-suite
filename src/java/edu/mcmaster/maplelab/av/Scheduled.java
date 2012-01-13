/**
 * 
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
