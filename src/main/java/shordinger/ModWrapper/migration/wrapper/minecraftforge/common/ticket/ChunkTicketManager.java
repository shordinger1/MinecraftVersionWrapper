/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package shordinger.ModWrapper.migration.wrapper.minecraftforge.common.ticket;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.ChunkPos;

public class ChunkTicketManager<T> implements ITicketGetter<T> {

    private final Set<SimpleTicket<T>> tickets = Collections.newSetFromMap(new WeakHashMap<>());
    public final ChunkPos pos;

    public ChunkTicketManager(ChunkPos pos) {
        this.pos = pos;
    }

    @Override
    public void add(SimpleTicket<T> ticket) {
        this.tickets.add(ticket);
    }

    @Override
    public void remove(SimpleTicket<T> ticket) {
        this.tickets.remove(ticket);
    }

    @Override
    public Collection<SimpleTicket<T>> getTickets() {
        return tickets;
    }
}