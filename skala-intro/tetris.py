# tetris.py
import pygame
import random
from typing import List, Tuple, Optional

# -----------------------
# Config
# -----------------------
CELL = 30
COLS, ROWS = 10, 20

PANEL_W = 220  # 좌/우 패널 폭
FIELD_W = COLS * CELL
FIELD_H = ROWS * CELL

WIDTH = PANEL_W + FIELD_W + PANEL_W
HEIGHT = FIELD_H

FPS = 60
FALL_INTERVAL_MS = 700
SOFT_DROP_MS = 50

BGM_FILE = "tetris_bgm.mp3"  # 같은 폴더에 넣기

# -----------------------
# Shapes (4x4)
# -----------------------
SHAPES = {
    "I": [
        [(0, 1), (1, 1), (2, 1), (3, 1)],
        [(2, 0), (2, 1), (2, 2), (2, 3)],
        [(0, 2), (1, 2), (2, 2), (3, 2)],
        [(1, 0), (1, 1), (1, 2), (1, 3)],
    ],
    "O": [
        [(1, 1), (2, 1), (1, 2), (2, 2)],
        [(1, 1), (2, 1), (1, 2), (2, 2)],
        [(1, 1), (2, 1), (1, 2), (2, 2)],
        [(1, 1), (2, 1), (1, 2), (2, 2)],
    ],
    "T": [
        [(1, 1), (0, 2), (1, 2), (2, 2)],
        [(1, 1), (1, 2), (2, 2), (1, 3)],
        [(0, 2), (1, 2), (2, 2), (1, 3)],
        [(1, 1), (0, 2), (1, 2), (1, 3)],
    ],
    "S": [
        [(1, 1), (2, 1), (0, 2), (1, 2)],
        [(1, 1), (1, 2), (2, 2), (2, 3)],
        [(1, 2), (2, 2), (0, 3), (1, 3)],
        [(0, 1), (0, 2), (1, 2), (1, 3)],
    ],
    "Z": [
        [(0, 1), (1, 1), (1, 2), (2, 2)],
        [(2, 1), (1, 2), (2, 2), (1, 3)],
        [(0, 2), (1, 2), (1, 3), (2, 3)],
        [(1, 1), (0, 2), (1, 2), (0, 3)],
    ],
    "J": [
        [(0, 1), (0, 2), (1, 2), (2, 2)],
        [(1, 1), (2, 1), (1, 2), (1, 3)],
        [(0, 2), (1, 2), (2, 2), (2, 3)],
        [(1, 1), (1, 2), (0, 3), (1, 3)],
    ],
    "L": [
        [(2, 1), (0, 2), (1, 2), (2, 2)],
        [(1, 1), (1, 2), (1, 3), (2, 3)],
        [(0, 2), (1, 2), (2, 2), (0, 3)],
        [(0, 1), (1, 1), (1, 2), (1, 3)],
    ],
}

COLORS = {
    "I": (0, 240, 240),
    "O": (240, 240, 0),
    "T": (160, 0, 240),
    "S": (0, 240, 0),
    "Z": (240, 0, 0),
    "J": (0, 0, 240),
    "L": (240, 160, 0),
}

GRID_BG = (18, 18, 18)
GRID_LINE = (35, 35, 35)
UI_BG = (12, 12, 12)

TEXT = (230, 230, 230)
SUBTEXT = (180, 180, 180)

# -----------------------
# Helpers
# -----------------------
def create_board() -> List[List[Optional[Tuple[int, int, int]]]]:
    return [[None for _ in range(COLS)] for _ in range(ROWS)]

def inside(x: int, y: int) -> bool:
    return 0 <= x < COLS and 0 <= y < ROWS

def can_place(board, shape_key: str, rot: int, px: int, py: int) -> bool:
    for (sx, sy) in SHAPES[shape_key][rot]:
        x, y = px + sx, py + sy
        if not inside(x, y):
            return False
        if board[y][x] is not None:
            return False
    return True

def lock_piece(board, shape_key: str, rot: int, px: int, py: int) -> None:
    color = COLORS[shape_key]
    for (sx, sy) in SHAPES[shape_key][rot]:
        x, y = px + sx, py + sy
        if inside(x, y):
            board[y][x] = color

def clear_lines(board) -> int:
    cleared = 0
    new_rows = []
    for row in board:
        if all(cell is not None for cell in row):
            cleared += 1
        else:
            new_rows.append(row)
    while len(new_rows) < ROWS:
        new_rows.insert(0, [None for _ in range(COLS)])
    board[:] = new_rows
    return cleared

def ghost_drop_y(board, shape_key, rot, px, py) -> int:
    y = py
    while can_place(board, shape_key, rot, px, y + 1):
        y += 1
    return y

def generate_bag() -> List[str]:
    bag = list(SHAPES.keys())
    random.shuffle(bag)
    return bag

# -----------------------
# UI helpers
# -----------------------
def draw_text(surface, font, text: str, x: int, y: int, color=TEXT):
    img = font.render(text, True, color)
    surface.blit(img, (x, y))

def draw_panel_bg(screen):
    # left panel
    pygame.draw.rect(screen, UI_BG, pygame.Rect(0, 0, PANEL_W, HEIGHT))
    # right panel
    pygame.draw.rect(screen, UI_BG, pygame.Rect(PANEL_W + FIELD_W, 0, PANEL_W, HEIGHT))

def draw_next_preview(screen, font, next_key: str):
    # Left top box
    box_x = 20
    box_y = 50
    box_size = 140
    pygame.draw.rect(screen, (25, 25, 25), pygame.Rect(box_x, box_y, box_size, box_size), border_radius=10)
    pygame.draw.rect(screen, (60, 60, 60), pygame.Rect(box_x, box_y, box_size, box_size), 2, border_radius=10)

    draw_text(screen, font, "NEXT", 20, 20, TEXT)

    # Draw 4x4 mini preview centered inside box
    # Use rotation 0 for preview
    cells = SHAPES[next_key][0]
    min_cell = 20  # preview cell size

    # Find bounds of occupied cells for centering
    xs = [c[0] for c in cells]
    ys = [c[1] for c in cells]
    minx, maxx = min(xs), max(xs)
    miny, maxy = min(ys), max(ys)
    w = (maxx - minx + 1) * min_cell
    h = (maxy - miny + 1) * min_cell

    start_x = box_x + (box_size - w) // 2
    start_y = box_y + (box_size - h) // 2

    color = COLORS[next_key]
    for (sx, sy) in cells:
        px = start_x + (sx - minx) * min_cell
        py = start_y + (sy - miny) * min_cell
        r = pygame.Rect(px, py, min_cell, min_cell)
        pygame.draw.rect(screen, color, r.inflate(-2, -2))

def draw_score_panel(screen, font, score: int, lines: int):
    # Right top texts
    base_x = PANEL_W + FIELD_W + 20
    draw_text(screen, font, "SCORE", base_x, 20, TEXT)
    draw_text(screen, font, f"{score}", base_x, 45, SUBTEXT)
    draw_text(screen, font, "LINES", base_x, 80, TEXT)
    draw_text(screen, font, f"{lines}", base_x, 105, SUBTEXT)

    # Controls hint
    small = pygame.font.SysFont("Arial", 14)
    y = 160
    draw_text(screen, small, "Controls:", base_x, y, TEXT)
    y += 20
    draw_text(screen, small, "← → : Move", base_x, y, SUBTEXT); y += 18
    draw_text(screen, small, "↑   : Rotate", base_x, y, SUBTEXT); y += 18
    draw_text(screen, small, "↓   : Soft drop", base_x, y, SUBTEXT); y += 18
    draw_text(screen, small, "Space: Hard drop", base_x, y, SUBTEXT)

# -----------------------
# Game
# -----------------------
class Piece:
    def __init__(self, shape_key: str):
        self.k = shape_key
        self.rot = 0
        self.x = (COLS // 2) - 2
        self.y = 0

    def color(self):
        return COLORS[self.k]

def try_play_bgm():
    # BGM 파일 없거나 코덱 문제면 그냥 스킵
    try:
        pygame.mixer.init()
        pygame.mixer.music.load(BGM_FILE)
        pygame.mixer.music.set_volume(0.4)
        pygame.mixer.music.play(-1)  # loop
    except Exception as e:
        # 콘솔에만 출력(게임은 계속)
        print(f"[BGM] skipped: {e}")

def main():
    pygame.init()
    screen = pygame.display.set_mode((WIDTH, HEIGHT))
    pygame.display.set_caption("Tetris (pygame)")

    clock = pygame.time.Clock()
    font = pygame.font.SysFont("Arial", 20)

    try_play_bgm()

    board = create_board()

    bag = generate_bag()
    def next_piece_key() -> str:
        nonlocal bag
        if not bag:
            bag = generate_bag()
        return bag.pop()

    current = Piece(next_piece_key())
    next_key = next_piece_key()

    if not can_place(board, current.k, current.rot, current.x, current.y):
        return

    fall_timer = 0
    fall_interval = FALL_INTERVAL_MS
    soft_drop = False

    score = 0
    lines = 0

    field_offset_x = PANEL_W
    field_offset_y = 0

    running = True
    while running:
        dt = clock.tick(FPS)
        fall_timer += dt

        # Input
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False

            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_LEFT:
                    if can_place(board, current.k, current.rot, current.x - 1, current.y):
                        current.x -= 1

                elif event.key == pygame.K_RIGHT:
                    if can_place(board, current.k, current.rot, current.x + 1, current.y):
                        current.x += 1

                elif event.key == pygame.K_DOWN:
                    soft_drop = True

                elif event.key == pygame.K_UP:
                    new_rot = (current.rot + 1) % 4  # clockwise
                    # simple wall kick attempts
                    for dx in [0, -1, 1, -2, 2]:
                        if can_place(board, current.k, new_rot, current.x + dx, current.y):
                            current.rot = new_rot
                            current.x += dx
                            break

                elif event.key == pygame.K_SPACE:
                    # hard drop
                    current.y = ghost_drop_y(board, current.k, current.rot, current.x, current.y)
                    lock_piece(board, current.k, current.rot, current.x, current.y)
                    c = clear_lines(board)
                    if c:
                        lines += c
                        score += [0, 100, 300, 500, 800][c]

                    # spawn next
                    current = Piece(next_key)
                    next_key = next_piece_key()
                    if not can_place(board, current.k, current.rot, current.x, current.y):
                        running = False

            elif event.type == pygame.KEYUP:
                if event.key == pygame.K_DOWN:
                    soft_drop = False

        fall_interval = SOFT_DROP_MS if soft_drop else FALL_INTERVAL_MS

        # Gravity
        if fall_timer >= fall_interval:
            fall_timer = 0
            if can_place(board, current.k, current.rot, current.x, current.y + 1):
                current.y += 1
            else:
                lock_piece(board, current.k, current.rot, current.x, current.y)
                c = clear_lines(board)
                if c:
                    lines += c
                    score += [0, 100, 300, 500, 800][c]

                current = Piece(next_key)
                next_key = next_piece_key()
                if not can_place(board, current.k, current.rot, current.x, current.y):
                    running = False

        # Draw
        screen.fill((0, 0, 0))
        draw_panel_bg(screen)

        # left: next preview
        draw_next_preview(screen, font, next_key)

        # right: score
        draw_score_panel(screen, font, score, lines)

        # field grid + locked blocks
        for y in range(ROWS):
            for x in range(COLS):
                rect = pygame.Rect(field_offset_x + x * CELL, field_offset_y + y * CELL, CELL, CELL)
                pygame.draw.rect(screen, GRID_BG, rect)
                pygame.draw.rect(screen, GRID_LINE, rect, 1)
                if board[y][x] is not None:
                    pygame.draw.rect(screen, board[y][x], rect.inflate(-2, -2))

        # ghost piece
        gy = ghost_drop_y(board, current.k, current.rot, current.x, current.y)
        ghost_color = tuple(min(255, c + 80) for c in current.color())
        for (sx, sy) in SHAPES[current.k][current.rot]:
            x, y = current.x + sx, gy + sy
            rect = pygame.Rect(field_offset_x + x * CELL, field_offset_y + y * CELL, CELL, CELL)
            pygame.draw.rect(screen, ghost_color, rect.inflate(-6, -6), 2)

        # current piece
        for (sx, sy) in SHAPES[current.k][current.rot]:
            x, y = current.x + sx, current.y + sy
            rect = pygame.Rect(field_offset_x + x * CELL, field_offset_y + y * CELL, CELL, CELL)
            pygame.draw.rect(screen, current.color(), rect.inflate(-2, -2))

        pygame.display.flip()

    # Game over
    try:
        pygame.mixer.music.stop()
    except Exception:
        pass

    screen.fill((0, 0, 0))
    over_font = pygame.font.SysFont("Arial", 26)
    msg1 = over_font.render("Game Over", True, (240, 240, 240))
    msg2 = font.render(f"Score: {score}   Lines: {lines}", True, (220, 220, 220))
    screen.blit(msg1, (WIDTH // 2 - msg1.get_width() // 2, HEIGHT // 2 - 40))
    screen.blit(msg2, (WIDTH // 2 - msg2.get_width() // 2, HEIGHT // 2 + 5))
    pygame.display.flip()

    waiting = True
    while waiting:
        for event in pygame.event.get():
            if event.type == pygame.QUIT or event.type == pygame.KEYDOWN:
                waiting = False
        clock.tick(30)

    pygame.quit()

if __name__ == "__main__":
    main()
