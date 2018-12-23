#include <AFMotor.h>
#include <SerialCommand.h>
byte currentDirection = 4;

const byte DIRECTION_FORWARD = 0;
const byte DIRECTION_BACKWARD = 1;
const byte DIRECTION_TURNLEFT = 3;
const byte DIRECTION_TURNRIGHT = 2;
const byte DIRECTION_NONE = 4;
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);

SerialCommand sCmd;

void setup() {
  Serial.begin(9600);
  setupMotor();
  addCommand();
}

void loop() {
  sCmd.readSerial();
}
void motorForward(){
  setMotorSpeed(255);
  motor3.run(FORWARD);
  motor4.run(FORWARD);
  currentDirection = DIRECTION_FORWARD;
}
void motorBackward(){
  setMotorSpeed(255);
  motor3.run(BACKWARD);
  motor4.run(BACKWARD);
  currentDirection = DIRECTION_BACKWARD;
}
void motorTurnLeft(){
  if(currentDirection == DIRECTION_FORWARD){
    motor3.setSpeed(64);
    motor4.setSpeed(255);
    
    motor3.run(FORWARD);
  }else{
    setMotorSpeed(154);
    motor3.run(BACKWARD);
  }
  motor4.run(FORWARD);
  currentDirection = DIRECTION_TURNLEFT;
}
void motorTurnRight(){
  if(currentDirection == DIRECTION_FORWARD){
    motor3.setSpeed(255);
    motor4.setSpeed(64);
    
    motor4.run(FORWARD);
  }else{
    setMotorSpeed(164);
    motor4.run(BACKWARD);
  }
  motor3.run(FORWARD);
  currentDirection = DIRECTION_TURNRIGHT;
}
void motorStop(){
  motor3.run(RELEASE);
  motor4.run(RELEASE);
  currentDirection = DIRECTION_NONE;
}
void setupMotor(){
  setMotorSpeed(255);
}
void setMotorSpeed(byte sp){
  motor3.setSpeed(sp);
  motor4.setSpeed(sp);
}
void addCommand(){
  sCmd.addCommand("1", motorForward);
  sCmd.addCommand("2", motorBackward);
  sCmd.addCommand("3", motorTurnLeft);
  sCmd.addCommand("4", motorTurnRight);
  sCmd.addCommand("5", motorStop);
}
