package pieces;

import java.awt.*;
import model.Block;
import model.Mino;

public class Mino_L1 extends Mino {
    public Mino_L1() {
        create(Color.orange);
    }

    public void setXY(int x, int y) {
        b[0].x = x;
        b[0].y = y;
        b[1].x = x;
        b[1].y = y - Block.SIZE;
        b[2].x = x;
        b[2].y = y + Block.SIZE;
        b[3].x = x + Block.SIZE;
        b[3].y = y + Block.SIZE;
    }

    public void getDirection1() {
        setTempBlocks(
            b[0].x, b[0].y,
            b[0].x, b[0].y - Block.SIZE,
            b[0].x, b[0].y + Block.SIZE,
            b[0].x + Block.SIZE, b[0].y + Block.SIZE
        );
        updateXY(1);
    }

    public void getDirection2() {
        setTempBlocks(
            b[0].x, b[0].y,
            b[0].x + Block.SIZE, b[0].y,
            b[0].x - Block.SIZE, b[0].y,
            b[0].x - Block.SIZE, b[0].y + Block.SIZE
        );
        updateXY(2);
    }

    public void getDirection3() {
        setTempBlocks(
            b[0].x, b[0].y,
            b[0].x, b[0].y + Block.SIZE,
            b[0].x, b[0].y - Block.SIZE,
            b[0].x - Block.SIZE, b[0].y - Block.SIZE
        );
        updateXY(3);
    }

    public void getDirection4() {
        setTempBlocks(
            b[0].x, b[0].y,
            b[0].x - Block.SIZE, b[0].y,
            b[0].x + Block.SIZE, b[0].y,
            b[0].x + Block.SIZE, b[0].y - Block.SIZE
        );
        updateXY(4);
    }

    private void setTempBlocks(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3) {
        tempB[0].x = x0; tempB[0].y = y0;
        tempB[1].x = x1; tempB[1].y = y1;
        tempB[2].x = x2; tempB[2].y = y2;
        tempB[3].x = x3; tempB[3].y = y3;
    }
}
