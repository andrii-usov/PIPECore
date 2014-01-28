package pipe.models.component.transition;

import pipe.models.component.Connectable;
import pipe.visitor.foo.PetriNetComponentVisitor;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;


public class Transition extends Connectable {

    /**
     * Message fired when the Transitions priority changes
     */
    public static final String PRIORITY_CHANGE_MESSAGE = "priority";

    /**
     * Message fired when the rate changes
     */
    public static final String RATE_CHANGE_MESSAGE = "rate";

    /**
     * Message fired when the angle changes
     */
    public static final String ANGLE_CHANGE_MESSAGE = "angle";

    /**
     * Message fired when the transition becomes timed/becomes immediate
     */
    public static final String TIMED_CHANGE_MESSAGE = "timed";

    /**
     * Message fired when the transition becomes an infinite/single server
     */
    public static final String INFINITE_SEVER_CHANGE_MESSAGE = "infiniteServer";

    /**
     * Message fired when the transition is enabled
     */
    public static final String ENABLED_CHANGE_MESSAGE = "enabled";

    /**
     * Mesasge fired when the transition is enabled
     */
    public static final String DISABLED_CHANGE_MESSAGE = "disabled";

    public static final int TRANSITION_HEIGHT = 30;

    public static final int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;

    private static final double ROOT_THREE_OVER_TWO = 0.5 * Math.sqrt(3);

    private int priority;

    private String rateExpr;

    private boolean timed = false;

    private boolean infiniteServer = false;

    private int angle = 0;

    //    private RateParameter rateParameter;
    private boolean enabled = false;

    public Transition(String id, String name) {
        this(id, name, "1", 1);
    }

    public Transition(String id, String name, String rateExpr, int priority) {
        super(id, name);
        this.rateExpr = rateExpr;
        this.priority = priority;
    }

    public Transition(Transition transition) {
        super(transition);
        this.infiniteServer = transition.infiniteServer;
        this.angle = transition.angle;
        this.timed = transition.timed;
        this.rateExpr = transition.rateExpr;
        this.priority = transition.priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        int old = this.priority;
        this.priority = priority;
        changeSupport.firePropertyChange(PRIORITY_CHANGE_MESSAGE, old, priority);
    }

    public String getRateExpr() {
        return rateExpr;
    }

    public void setRateExpr(double expr) {
        rateExpr = Double.toString(expr);
    }

    public void setRateExpr(String string) {
        rateExpr = string;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        int old = this.angle;
        this.angle = angle;
        changeSupport.firePropertyChange(ANGLE_CHANGE_MESSAGE, old, angle);
    }

    public boolean isTimed() {
        return timed;
    }

    public void setTimed(boolean timed) {
        boolean old = this.timed;
        this.timed = timed;
        changeSupport.firePropertyChange(TIMED_CHANGE_MESSAGE, old, timed);
    }

    public boolean isInfiniteServer() {
        return infiniteServer;
    }

    public void setInfiniteServer(boolean infiniteServer) {
        boolean old = this.infiniteServer;
        this.infiniteServer = infiniteServer;
        changeSupport.firePropertyChange(INFINITE_SEVER_CHANGE_MESSAGE, old, infiniteServer);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof TransitionVisitor) {
            ((TransitionVisitor) visitor).visit(this);
        }
    }

    public void enable() {
        enabled = true;
        changeSupport.firePropertyChange(ENABLED_CHANGE_MESSAGE, false, true);
    }

    public void disable() {
        enabled = false;
        changeSupport.firePropertyChange(DISABLED_CHANGE_MESSAGE, true, false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + priority;
        result = 31 * result + rateExpr.hashCode();
        result = 31 * result + (timed ? 1 : 0);
        result = 31 * result + (infiniteServer ? 1 : 0);
        result = 31 * result + angle;
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Transition that = (Transition) o;

        if (!super.equals(that)) {
            return false;
        }

        if (angle != that.angle) {
            return false;
        }
        if (enabled != that.enabled) {
            return false;
        }
        if (infiniteServer != that.infiniteServer) {
            return false;
        }
        if (priority != that.priority) {
            return false;
        }
        if (timed != that.timed) {
            return false;
        }
        if (!rateExpr.equals(that.rateExpr)) {
            return false;
        }

        return true;
    }

    @Override
    public Point2D.Double getCentre() {
        return new Point2D.Double(getX() + getHeight() / 2, getY() + getHeight() / 2);
    }

    //    public RateParameter getRateParameter() {
    //        return rateParameter;
    //    }

    //    public void setRateParameter(RateParameter rateParameter) {
    //        this.rateParameter = rateParameter;
    //    }

    @Override
    public Point2D.Double getArcEdgePoint(double angle) {
        double half_height = getHeight() / 2;
        double centre_x =
                x + half_height; //Use height since the actual object is a square, width is just the displayed width
        double centre_y = y + half_height;

        Point2D.Double connectionPoint = new Point2D.Double(centre_x, centre_y);

        double half_width = getWidth() / 2;
        double rotatedAngle = angle + Math.toRadians(this.angle);
        if (connectToTop(rotatedAngle)) {
            connectionPoint.y -= half_height;
        } else if (connectToBottom(rotatedAngle)) {
            connectionPoint.y += half_height;
        } else if (connectToLeft(rotatedAngle)) {
            connectionPoint.x -= half_width;
        } else { //connectToRight
            connectionPoint.x += half_width;
        }

        return rotateAroundCenter(Math.toRadians(this.angle), connectionPoint);
    }

    /**
     * Rotates point on transition around transition center
     *
     * @param angle rotation angle in degrees
     * @param point point to rotate
     * @return rotated point
     */
    private Point2D.Double rotateAroundCenter(double angle, Point2D.Double point) {
        AffineTransform tx = new AffineTransform();
        Point2D center = getCentre();
        tx.rotate(angle, center.getX(), center.getY());
        Point2D.Double rotatedPoint = new Point2D.Double();
        tx.transform(point, rotatedPoint);
        return rotatedPoint;
    }

    @Override
    public boolean isEndPoint() {
        return true;
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the left edge of the transition
     */
    private boolean connectToLeft(double angle) {
        return (Math.sin(angle) > 0);
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should connect to the bottom edge
     * of the transition
     */
    private boolean connectToBottom(double angle) {
        return Math.cos(angle) < -ROOT_THREE_OVER_TWO;
    }

    /**
     * @param angle in radians
     * @return true if an arc connecting to this should
     * connect to the top edge of the transition
     */
    private boolean connectToTop(double angle) {
        return Math.cos(angle) > ROOT_THREE_OVER_TWO;
    }

    @Override
    public int getHeight() {
        return TRANSITION_HEIGHT;
    }

    @Override
    public int getWidth() {
        return TRANSITION_WIDTH;
    }
}
