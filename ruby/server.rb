require 'socket'
require 'thread'
require './message'

class Server
  def initialize
    @server = TCPServer.new('localhost', 39090)
    @messages = Queue.new
    @clients = {}
  end

  def start
    puts "started server"
    while(true) do 
      Thread.start(@server.accept) do |client|
        listen_to(client)
      end
    end
  end

  def listen_to(client)
    while(true) do 
      m = Message.try_convert(JSON.load(client.gets))
      if m.signing_in?
        add_client(m.from, client)
      else
        if m.signing_off?
          remove_client(m.from)
        end
        @messages << m
      end
      puts @messages.inspect
      publish if @clients.size > 1 && @messages.size > 0
    end
  end

  def publish
    until @messages.empty?
      message = @messages.shift
      @clients.reject{|k,v| k == message.from}.each do |name, socket|
        socket.puts(message.to_json)
      end
    end
  end

  def add_client(name, socket)
    unless @clients.has_key?(name)
      @clients[name] = socket
    end
  end

  def remove_client(name)
    @clients[name] = nil
  end
end

Server.new.start
