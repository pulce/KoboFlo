package com.koboflo;

/**
 * 
 * This Class is part of KoboFlo.
 * 
 * It adds an About Dialog to the given context.
 * 
 * @author Florian Hauser Copyright (C) 2014
 * 
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 * 
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class Message {
	private String icon;
	private String init;
	private int message;

	public Message(String init, int message, String icon) {
		this.init = init;
		this.message = message;
		this.icon = icon;
	}

	public String getIcon() {
		return this.icon;
	}

	public String getInit() {
		return this.init;
	}

	public int getMessage() {
		return this.message;
	}
}
