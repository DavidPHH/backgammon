Backgammon Bot
-------------------------------------------------------------------------------------------------------------------------------------------

Plan
Doubling: Check the % differenec in pip numbers. If % is in favour of opponent by a sizeable amount then reject double, if % is in favour of
you by a large margin, then double.
Crawford Rule: Losing player should always double after this rule.

Selecting the move:
Give each possible board state after a possble move a score. Perform the best scoring move.

Scoring the moves: 
Give a different score to: bear-off moves, bar moves, normal moves, hit moves and creating a block, creating a prime (block wall).

Checking what the move was: 
Compare current board state to the hypothetical board state. If it was a bear-off move, then the hypothetical state will have more pieces in
the bear-off, but could possibly leave a blot (which would be a negative). 

Theoretical scores: Give a co-efficient to each move, then add them all up to give a score.

Standardise the score: 
Make it so that each type of move(s) will have a max and min range of scores.
Make it easier to give a probability of winning.

------------------------------------------------------------------------------------------------
Choose coefficients for PipDiff, BlotDiff, BlockDiff, and BearOffDiff
(c = coefficient)
score = (cPip * PipDiff) + (cBlot * BlotDiff) + (cBlock * BlockDiff) + (cBear * BearOffDiff)


relativePipDiff(Board, me, opponent){ 
	Pipsme - PipsOpponent / Pipsme + PipsOpponent

	return 0 to 100 score
}

diffOfBlots(Board, me, opponent){
	Blotsme - BlotspOpponent
	
	return 0 to 100 score	
}

diffOfBlocks(Board, me, opponent){
	Blocksme - BlocksOpponent	
	
	return 0 to 100 score
}

diffOffBearOFf(Board, me, opponent){

}

willDouble(){

}
