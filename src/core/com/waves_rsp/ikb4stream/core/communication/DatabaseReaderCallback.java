/*
 * Copyright (C) 2017 ikb4stream team
 * ikb4stream is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ikb4stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.waves_rsp.ikb4stream.core.communication;

/**
 * FunctionalInterface use after reading in database
 *
 * @author ikb4stream
 * @version 1.0
 */
@FunctionalInterface
public interface DatabaseReaderCallback {
    /**
     * Method call after reading in database
     *
     * @param t      Throwable in case of error during reading
     * @param result Result of database request
     */
    void onResult(Throwable t, String result);
}
