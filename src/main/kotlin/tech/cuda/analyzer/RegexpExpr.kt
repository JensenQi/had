/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.cuda.analyzer

import tech.cuda.enums.ColumnType
import java.util.*

/**
 * expression for REGEXP.
 * arg must evaluate to char, varchar or text.
 */
class RegexpExpr(val arg: Expression, val patternExpr: Expression, val escapeExpr: Expression?)
    : Expression(ColumnType.BOOLEAN) {

    override fun groupPredicates(
            scanPredicates: MutableList<Expression>,
            joinPredicates: MutableList<Expression>,
            constPredicates: MutableList<Expression>) {
        val rteIdxSet: MutableSet<Int> = HashSet()
        arg.collectRangeTableEntriesIndexTo(rteIdxSet)
        when {
            rteIdxSet.size > 1 -> joinPredicates.add(this)
            rteIdxSet.size == 1 -> scanPredicates.add(this)
            else -> constPredicates.add(this)
        }
    }

    override fun collectRangeTableEntriesIndexTo(rteSet: MutableSet<Int>) {
        arg.collectRangeTableEntriesIndexTo(rteSet)
    }

    override fun collectColumnVar(colVarSet: TreeSet<ColumnVar>, hasAgg: Boolean) {
        arg.collectColumnVar(colVarSet, hasAgg)
    }

    override fun rewriteWithTargetList(targetList: MutableList<TargetEntry>): Expression {
        return RegexpExpr(arg.rewriteWithTargetList(targetList),
                patternExpr.deepCopy(),
                escapeExpr?.deepCopy())
    }

    override fun rewriteWithChildTargetList(targetList: MutableList<TargetEntry>): Expression {
        return RegexpExpr(arg.rewriteWithChildTargetList(targetList),
                patternExpr.deepCopy(),
                escapeExpr?.deepCopy())
    }

    override fun rewriteAggToVar(targetList: MutableList<TargetEntry>): Expression {
        return RegexpExpr(arg.rewriteAggToVar(targetList),
                patternExpr.deepCopy(),
                escapeExpr?.deepCopy())
    }

    override fun deepCopy(): Expression {
        return RegexpExpr(arg.deepCopy(), patternExpr.deepCopy(), escapeExpr?.deepCopy())
    }

    override fun toString(): String {
        return "(REGEXP $arg $patternExpr) "
    }

    override fun findExpression(matchFunc: (Expression) -> Boolean, exprList: ExpressionList) {
        if (matchFunc(this)) {
            addUnique(exprList)
        } else {
            arg.findExpression(matchFunc, exprList)
            patternExpr.findExpression(matchFunc, exprList)
            escapeExpr?.findExpression(matchFunc, exprList)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegexpExpr

        if (arg != other.arg) return false
        if (patternExpr != other.patternExpr) return false
        if (escapeExpr != other.escapeExpr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arg.hashCode()
        result = 31 * result + patternExpr.hashCode()
        result = 31 * result + (escapeExpr?.hashCode() ?: 0)
        return result
    }

}
