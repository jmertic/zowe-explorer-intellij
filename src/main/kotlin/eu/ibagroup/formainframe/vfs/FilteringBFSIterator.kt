/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.vfs

import org.jgrapht.Graph
import org.jgrapht.traverse.BreadthFirstIterator

open class FilteringBFSIterator<Vertex, Edge>(
  g: Graph<Vertex, Edge>,
  startVertices: MutableIterable<Vertex>?,
  private val predicate: (Vertex, Edge) -> Boolean
) : BreadthFirstIterator<Vertex, Edge>(g, startVertices) {

  constructor(g: Graph<Vertex, Edge>, predicate: (Vertex, Edge) -> Boolean) : this(g, null, predicate)

  constructor(g: Graph<Vertex, Edge>, startVertex: Vertex, predicate: (Vertex, Edge) -> Boolean) : this(
    g,
    mutableListOf(startVertex),
    predicate
  )

  override fun encounterVertex(vertex: Vertex, edge: Edge?) {
    if (edge == null || predicate(vertex, edge)) {
      super.encounterVertex(vertex, edge)
    }
  }

  override fun encounterVertexAgain(vertex: Vertex, edge: Edge?) {
    if (edge == null || predicate(vertex, edge)) {
      super.encounterVertexAgain(vertex, edge)
    }
  }

}