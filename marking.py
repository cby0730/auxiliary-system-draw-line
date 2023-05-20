from PIL import Image, ImageDraw, ImageFont
import sys, math
from queue import Queue

targetColors = [(0,0,192)]
thresholdPixel = 300 
dy = [0,1,0,-1]
dx = [1,0,-1,0]

circleRadius = 5
centerColors = [(0,255,0)]
smallCircleRadius = 0

def drawCenter(img):
    pix = img.load()
    w,h = img.size
    vis = [[False for i in range(h)] for j in range(w) ]
    centerPos = []
    for i in range(w):
        for j in range(h):
            if vis[i][j]: continue;
            for t in range( len( targetColors ) ):
                if pix[i,j] != targetColors[t]: continue
                pos, area = calcCenterPos( img, (i,j), vis )
                if area >= thresholdPixel:
                    drawCircle( img, pos, circleRadius, centerColors[t] )
                    centerPos.append( pos )
    return centerPos

def drawCircle(img, pos, radius, color ):
    w,h = img.size
    for i in range( -radius, radius+1 ) :
        for j in range( -radius, radius+1 ) :
            if i*i + j*j <= radius**2:
                x = pos[0]+i
                y = pos[1]+j;
                if x >= 0 and y >= 0 and x < w and y < h: img.putpixel( (x,y), color )
    

def drawEdge(img):
    pix = img.load()
    w,h = img.size
    for t in range( len( targetColors ) ):
        for i in range(w):
            j = 0
            while j < h:
                while j < h and pix[i,j] != targetColors[t]: j += 1
                if j == h: break
                temp = (i,j)
                while j < h and pix[i,j] == targetColors[t]: j += 1
                drawCircle( img, *temp, smallCircleRadius, centerColors[t] )
                if j < h: drawCircle( img,i,j,smallCircleRadius, centerColors[t] )

def judgeFloor(img):
    pix = img.load()
    w,h = img.size
    if pix[w//2, h-1] != targetColors[0]: return False
    _, area = calcCenterPos( img, (w//2, h-1) )
    return area >= thresholdPixel

def calcCenterPos(img, pos, vis = None): 
    pix = img.load()
    w,h = img.size
    clr = pix[pos]
    q = Queue()
    q.put( pos )
    if vis == None: vis = [[False for i in range(h)] for j in range(w) ]
    vis[pos[0]][pos[1]] = True
    sumw = 0
    sumh = 0
    sump = 0
    while q.qsize():
        x,y = q.get()
        sumw += x
        sumh += y
        sump += 1
        for k in range(4):
            nx = x+dx[k]
            ny = y+dy[k]
            if nx >= 0 and ny >= 0 and nx < w and ny < h and not vis[nx][ny] and pix[nx,ny] == clr :
                q.put( (nx,ny ) )
                vis[nx][ny] = True
    return ( sumw // sump, sumh // sump ), sump

arrowImg = Image.open("arrow.png").convert('RGBA').resize( (50,50) )
def drawArrow(img, angle):
    pix = img.load()
    print(angle)
    rotatedArrowImg = arrowImg.rotate( angle )
    w,h = img.size
    if pix[w//2, h-1] == targetColors[0]: pos, _ = calcCenterPos(img, (w//2, h-1) )
    else: pos = ( w//2, h-1 )
    centerPos = ( pos[0] - arrowImg.size[0] // 2, pos[1] - arrowImg.size[1] // 2 )
    offsetPos = ( centerPos[0], centerPos[1] - 25 )
    img.paste(rotatedArrowImg,offsetPos,rotatedArrowImg.convert('RGBA'))


fontSize = 24
def drawInfo( img, dist, name, turn, ori, is_walkable, traffic_light, pointAngle ):

    draw = ImageDraw.Draw( img )
    font = ImageFont.truetype( 'TaipeiSansTCBeta-Regular.ttf', fontSize )
    if name == 'null' : name = 'The Road has no name'
    if dist <= 15 and turn != 'END' :
        draw.text( (5,5), "This Road is head to %d°"%ori, font = font, align = 'left' )
        draw.text( (5,30), "Distance %dm"%dist, font = font, align = 'left' )
        draw.text( (5,55), "Next Road's Name : %s"%name, font = font, align = 'left' )
        draw.text( (5,80), "Next Turn is %s"%turn, font = font, align = 'left' )

    elif turn == 'END' :
        draw.text( (5,5), "This Road is head to %d°"%ori, font = font, align = 'left' )
        draw.text( (5,30), "Distance %dm"%dist, font = font, align = 'left' )
        draw.text( (5,55), "Next Road's Name : No next road", font = font, align = 'left' )
        draw.text( (5,80), "Your about to the Destination", font = font, align = 'left' )

    else :
        draw.text( (5,5), "This Road is head to %d°"%ori, font = font, align = 'left' )
        draw.text( (5,30), "Distance %dm"%dist, font = font, align = 'left' )
        draw.text( (5,55), "Next Road's Name : %s"%name, font = font, align = 'left' )
        draw.text( (5,80), "Next Turn is %s"%turn, font = font, align = 'left' )
    
    if is_walkable :
        draw.text( (5,105), "You are in sidewalk area", font = font, align = 'left' )
    else :
        draw.text( (5,105), "You are not in sidewalk area", font = font, align = 'left' )
    
    draw.text( (5,130), "Traffic light : %s"%traffic_light, font = font, align = 'left' )
    
    if pointAngle != None:
        draw.text( (5, 155 ), "Angle to next sidewalk point: {}".format(pointAngle), font = font, align = 'left' )

import read_txt as rt
import cv2
from voice import say
def drawAndSave(pic, traffic_light):
    img = Image.open(pic).convert('RGB')
    is_walkable = judgeFloor(img)
    centerPos = drawCenter( img )

    dist, name, angel, ori, turn = rt.Recent_info() #read navigation informations
    
    if dist <= 15 and turn != 'END' :
        drawArrow(img, angel)
    else :
        drawArrow(img, 0)

    pointAngle = None
    if centerPos: pointAngle = drawLineToPoint( img, centerPos[-1] )
    if pointAngle != None:
        if pointAngle <= -20:
            say('turn left')
        elif pointAngle >= 20:
            say('turn right')
            
    drawInfo( img, dist, name, turn, ori, is_walkable, traffic_light, pointAngle )
    print("yes")
    img.save("%s_marked.png"%pic.split('.')[0])
    

def drawLineToPoint( img, to ):
    id = ImageDraw.Draw( img )
    w,h = img.size
    lineWidth = 5;
    midBottomPos = ( w//2, h )
    if( to[0] * 2 < w ): 
        id.line((to, midBottomPos), width=lineWidth, fill=centerColors[0] )
    else: 
        id.line((midBottomPos,to),width=lineWidth,fill=centerColors[0]) 
    
    veca = (0,-1)
    vecb = (to[0] - midBottomPos[0], to[1] - midBottomPos[1] )
    adotb = veca[0]*vecb[0] + veca[1]*vecb[1]
    absb = math.sqrt( vecb[0]*vecb[0] + vecb[1]*vecb[1] )
    cosX = adotb / absb
    X = math.acos(cosX)
    deg = X / math.pi * 180
    normalDeg = deg if deg < 180 else deg-360
    return math.round( normalDeg )

    
        
def test():
    drawAndSave( sys.argv[1], "g" )


if __name__ == '__main__':
    test()
