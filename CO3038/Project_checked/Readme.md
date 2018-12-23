# Điều khiển robot 3 bánh mini thông qua internet có camera hành trình

Project sử dụng ngôn ngữ : C, Python và Java/ Kotlin

# Cài đặt
1. [Arduino](#Arduino)
2. [Android](#Android)
3. [Raspberry](#Raspberry)

## Arduino
1. Cài đặt Arduino Software
#### Link tải: https://www.arduino.cc/en/Main/Software
2. Cài đặt thư viện AFMotor
#### Link tải: http://k2.arduino.vn/img/2015/05/27/0/1406_882450-1432722684-0-af-motor.zip
3. Cài đặt thư viện SerialCommand
#### Link tải: https://github.com/kroimon/Arduino-SerialCommand
4. Kết nối mạch arduino vào PC và nạp code.
```arduino
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
```
Chú thích: Đoạn code android trên cho phép điều khiển chiếc xe thông qua Serial Command.

## Android
1. Tải source code về
```bash
git clone https://github.com/minhtu9x/Android-Things.git
```
Tìm thư mục Android.
2. Build lại project và nạp vào điện thoại

## Raspberry
Yêu cầu cài hệ điều hành raspbian.
1. Tải source code về
```bash
git clone https://github.com/minhtu9x/Android-Things.git
```
Tìm thư mục Raspberry.
2. Copy hết file python vào thư một thư mục.

# Sử dụng
Kết nối Android và Raspberry vào chung 1 mạng wifi.

1. Arduino: Nạp code xong là xong.
2. Raspberry: SSH vào thư mục bằng 2 tab.
Chạy 2 command sau bằng 2 tab.
* Tab 1:
```bash
python3 camera.py
```
* Tab 2
```bash
python dieukhien.py
```
3. Androd chạy app và điền ip của raspberry vào và bắt đầu điều khiển.

# Đóng góp
Luôn sẵn sàng lắng nghe ý kiến đóng góp từ mọi người.

# Giấy phép
[MIT](https://choosealicense.com/licenses/mit/)
