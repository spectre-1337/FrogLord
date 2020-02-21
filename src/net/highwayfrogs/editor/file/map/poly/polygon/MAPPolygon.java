package net.highwayfrogs.editor.file.map.poly.polygon;

import lombok.Getter;
import net.highwayfrogs.editor.file.map.poly.MAPPrimitive;
import net.highwayfrogs.editor.file.map.view.MapMesh;
import net.highwayfrogs.editor.file.reader.DataReader;
import net.highwayfrogs.editor.file.standard.SVector;
import net.highwayfrogs.editor.file.writer.DataWriter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents Playstation polygon data.
 * Created by Kneesnap on 8/25/2018.
 */
@Getter
public abstract class MAPPolygon extends MAPPrimitive {
    private short padding;

    public static final int TRI_SIZE = 3;
    public static final int QUAD_SIZE = 4;
    public static final int REQUIRES_VERTEX_PADDING = TRI_SIZE;
    public static final int REQUIRES_VERTEX_SWAPPING = QUAD_SIZE;

    public MAPPolygon(MAPPolygonType type, int verticeCount) {
        super(type, verticeCount);
    }

    @Override
    public MAPPolygonType getType() {
        return (MAPPolygonType) super.getType();
    }

    @Override
    public void load(DataReader reader) {
        super.load(reader);
        if (getVerticeCount() == REQUIRES_VERTEX_PADDING)
            this.padding = reader.readShort(); // Padding? This value seems to sometimes match the last vertices element, and sometimes it doesn't. I don't believe this value is used. Most likely it is whatever value was there when malloc was run, but we preserve it just in case it's important.
    }

    @Override
    public void save(DataWriter writer) {
        super.save(writer);
        if (getVerticeCount() == REQUIRES_VERTEX_PADDING)
            writer.writeShort(this.padding);
    }

    /**
     * Convert this into a wavefront object face command.
     * @return faceCommand
     */
    public String toObjFaceCommand(boolean showTextures, AtomicInteger textureCounter) {
        StringBuilder builder = new StringBuilder("f");
        for (int i = getVerticeCount() - 1; i >= 0; i--) {
            builder.append(" ").append(getVertices()[i] + 1);
            if (showTextures)
                builder.append("/").append(textureCounter != null ? textureCounter.incrementAndGet() : 0);
        }
        return builder.toString();
    }

    /**
     * Get the order this should be put in a .obj file.
     * @return orderId
     */
    public int getOrderId() {
        return 0;
    }

    /**
     * Calculate geometric center point of a polygon.
     * @return Center of a polygon, else null.
     */
    public static SVector getCenterOfPolygon(MapMesh mesh, MAPPolygon poly) {
        if (poly == null)
            return null;

        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        for (int index : poly.getVertices()) {
            x += mesh.getVertices().get(index).getFloatX();
            y += mesh.getVertices().get(index).getFloatY();
            z += mesh.getVertices().get(index).getFloatZ();
        }

        int count = poly.getVertices().length;
        if (count != 0) {
            x /= count;
            y /= count;
            z /= count;
        }

        return new SVector(x, y, z);

    }
}
