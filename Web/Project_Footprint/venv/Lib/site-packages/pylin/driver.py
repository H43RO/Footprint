# This class implements the "driver" object, that allows for communication with a Lin Engineering stepper motor driver.

import serial # PySerial
import time

class driver:

    # Constructor
    def __init__(self,portname,address=1):
        self.portname = portname # name of serial port in use
        self.address = str(address) # address of the contoller (default is 1)

        # some default values, from Lin
        self.m = 30 # running current (% max deliverable to motor)
        self.h = 10 # holding current
        self.j = 256 # step resolution
        self.V = 305175 # microsteps / second
        self.L = 1000 # acceleration
        self.o = 1500 # microstep smoothness
        self.b = 9600 # baud rate
        self.debug = False

        self.tc = '\r' # termination character for command strings
        self.sleeptime = 0.333 # sleep time between issuing serial port command and closing port. Default was determined empirically, may need adjusting.
    
    # Set address (make this driver interact with a different physical motor driver)
    def SetAddress(self,address=1):
        self.address = str(address)

    # Run a command
    def RunCommand(self,command_string):
        ser = serial.Serial(self.portname,timeout=0)
        ser.flushInput()
        ser.flushOutput()
        if self.debug:
            print('command_string is \'' + command_string + '\'')
        ser.write(bytes(command_string,'utf8'))
        time.sleep(self.sleeptime) # avoid closing the serial port mid-command.
        ser.close()
        time.sleep(self.sleeptime)


    # Create command string
    def MakeCommand(self,input_string,save=False):
        if save:
            return '/'+ self.address + input_string + 'X' + self.tc
        else:
            return '/'+ self.address + input_string + 'R' + self.tc

    # Get running parameters
    def GetParams(self):
        print('m = ' + str(self.m) + '(running current (% max deliverable to motor), default = 30)')
        print('h = ' + str(self.h) + '(holding current (% max deliverable to motor), default = 10)')
        print('j = ' + str(self.j) + '(step resolution, default = 256)')
        print('V = ' + str(self.V) + '(microsteps / second, default = 305175)')
        print('L = ' + str(self.L) + '(acceleration, default = 1000)')
        print('o = ' + str(self.o) + '(microstep smoothness, default = 1500)')
        print('b = ' + str(self.b) + '(baud rate, default = 9600)')
        print('tc = ' + str(self.tc) + '(running current (% max deliverable to motor))')
        print('sleeptime = ' + str(self.sleeptime) + '(running current (% max deliverable to motor))')
        print('To adjust running parameters, you can call SetParams(m,h,j,V,L,o,b) (or adjust any of these separately).')

    # Set running parameters
    def SetParams(self,m=-1,h=-1,j=-1,V=-1,L=-1,o=-1,b=-1):
        
        if m != -1:
            self.m = m
        if h != -1:
            self.h = h
        if j != -1:
            self.j = j
        if V != -1:
            self.V = V
        if L != -1:
            self.L = L
        if o != -1:
            self.o = o
        if b != -1:
            self.b = b
        self.RunCommand(self.MakeCommand('m' + str(self.m) + 'h' + str(self.h) + 'j' + str(self.j) + 'V' + str(self.V) + 'L' + str(self.L) + 'o' + str(self.o) + 'b' + str(self.b)))

    # Clear controller memory
    def ClearMemory(self):
        self.RunCommand(self.MakeCommand('?9'))

    # Step forward/backward x microsteps, x in range x = {0,2^31}
    def Step(self, x, forward = True):
        x = (int(x))%(2**31)

        if self.debug:
            print('x = ' + str(x))
        # if moving backwards, we will reverse polarity.
        # alternatively we can use the Lin "D" command,
        # but this is less reliable as it will not
        # work if we happen to be at position zero.
        if forward == False:
            self.RunCommand(self.MakeCommand('F1'))

        self.RunCommand(self.MakeCommand('P'+ str(x)))

        # reverse polarity again if we did it before
        if forward == False:
            self.RunCommand(self.MakeCommand('F0'))

    # Move to absolute position x, x in range x = {0,2^31}
    def MoveTo(self, x):
        x = (int(x))%(2**31)
        self.RunCommand(self.MakeCommand('A' + str(x)))

    # Define current position as position x, x in range x = {0,2^31}
    def SetPosition(self,x):
        x = (int(x))%(2**31)
        self.RunCommand(self.MakeCommand('z' + str(x)))


