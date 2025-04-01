package com.example.demo1.core;

/**
 * Class representing a chess move
 */
public class Move {

    private Position source;
    private Position target;
    private ChessPieceType promotionType;

    // Castling properties
    private boolean isCastling = false;
    private Position castlingRookSource = null;
    private Position castlingRookTarget = null;

    /**
     * Constructor for move
     *
     * @param source Source position
     * @param target Target position
     */
    public Move(Position source, Position target) {
        this(source, target, null);
    }

    /**
     * Constructor for move with promotion
     *
     * @param source        Source position
     * @param target        Target position
     * @param promotionType Type to promote to (for pawn promotion)
     */
    public Move(Position source, Position target, ChessPieceType promotionType) {
        this.source = source;
        this.target = target;
        this.promotionType = promotionType;
    }

    /**
     * Get the source position
     *
     * @return Source position
     */
    public Position getSource() {
        return source;
    }

    /**
     * Get the target position
     *
     * @return Target position
     */
    public Position getTarget() {
        return target;
    }

    /**
     * Get the promotion type
     *
     * @return Type to promote to, or null if no promotion
     */
    public ChessPieceType getPromotionType() {
        return promotionType;
    }

    /**
     * Check if this is a castling move
     *
     * @return true if this is a castling move
     */
    public boolean isCastling() {
        return isCastling;
    }

    /**
     * Set this move as a castling move
     *
     * @param castling Whether this is a castling move
     */
    public void setCastling(boolean castling) {
        this.isCastling = castling;
    }

    /**
     * Get the source position of the rook for a castling move
     *
     * @return Position of the rook before castling
     */
    public Position getCastlingRookSource() {
        return castlingRookSource;
    }

    /**
     * Set the source position of the rook for a castling move
     *
     * @param rookSource Position of the rook before castling
     */
    public void setCastlingRookSource(Position rookSource) {
        this.castlingRookSource = rookSource;
    }

    /**
     * Get the target position of the rook after castling
     *
     * @return Position of the rook after castling
     */
    public Position getCastlingRookTarget() {
        return castlingRookTarget;
    }

    /**
     * Set the target position of the rook for a castling move
     *
     * @param rookTarget Position of the rook after castling
     */
    public void setCastlingRookTarget(Position rookTarget) {
        this.castlingRookTarget = rookTarget;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Move other = (Move) obj;

        if (!source.equals(other.source) || !target.equals(other.target)) {
            return false;
        }

        if (isCastling != other.isCastling) {
            return false;
        }

        if (isCastling) {
            return (castlingRookSource.equals(other.castlingRookSource) &&
                    castlingRookTarget.equals(other.castlingRookTarget));
        }

        return promotionType == other.promotionType ||
                (promotionType != null && promotionType.equals(other.promotionType));
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + (promotionType != null ? promotionType.hashCode() : 0);
        if (isCastling) {
            result = 31 * result + castlingRookSource.hashCode();
            result = 31 * result + castlingRookTarget.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (isCastling) {
            // Use standard O-O for kingside and O-O-O for queenside castling
            boolean isKingside = target.getFile() == 'g';
            return isKingside ? "O-O" : "O-O-O";
        }

        return source.toString() + target.toString() +
                (promotionType != null ? promotionType.toString().substring(0, 1) : "");
    }
}
